from app.utils.error_handler import handle_errors, UnprocessableError, ValidationError
from app.repositories.user_repo import UserRepository
from app.services.user_service import UserService
from app.services.subscription_service import SubscriptionService
from flask import jsonify
from app.utils.firebase_utils import FirebaseUtils
from app.utils.jwt_utils import JwtUtils


class FirebaseAuthService:
    @staticmethod
    @handle_errors
    def firebase_auth(data):
        firebase_id_token = data.get('firebase_id_token')
        if not firebase_id_token:
            raise ValidationError("firebase_id_token is required for firebase authentication.")

        decoded_firebase_id_token = FirebaseUtils.verify_token(firebase_id_token)
        if not decoded_firebase_id_token:
            raise UnprocessableError("Invalid firebase_id_token.")

        user_email = decoded_firebase_id_token.get('email')
        user_name = decoded_firebase_id_token.get('name')
        user_google_id = decoded_firebase_id_token.get('sub')

        user = UserRepository.get_by_email(user_email)
        if user:
            if not user.google_id:
                UserService.add_google_data(user, user_google_id)
            token = JwtUtils.generate_jwt({'user_id': str(user.user_id)})
        else:
            data = {'name': user_name, 'email': user_email, 'google_id': user_google_id}
            user = UserService.google_register_user(data, 'user')
            SubscriptionService.create_basic_subscription(user.user_id)
            UserService.verify_email(user)

            token = JwtUtils.generate_jwt({'user_id': str(user.user_id)})

        return jsonify({'message': 'Firebase logged in successfully.', 'token': token}), 200
