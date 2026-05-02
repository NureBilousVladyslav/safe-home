from firebase_admin import auth


class FirebaseUtils:

    @staticmethod
    def verify_token(firebase_id_token):
        try:
            decoded_token = auth.verify_id_token(firebase_id_token)
            return decoded_token
        except auth.InvalidIdTokenError:
            return None
