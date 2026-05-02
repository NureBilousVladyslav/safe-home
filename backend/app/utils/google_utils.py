from app.utils import ErrorHandler
import requests


class GoogleUtils:

    @staticmethod
    def get_user_info(access_token):
        url = 'https://www.googleapis.com/oauth2/v3/userinfo'
        headers = {'Authorization': f'Bearer {access_token}'}

        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            return response.json()
        else:
            raise RuntimeError(f"Error fetching user info: {response.text}")

    @staticmethod
    def get_fresh_google_access_token(user, google):

        if not user.google_refresh_token:
            raise ValueError("Refresh token not available for the user.")

        refresh_token = user.get_refresh_token()

        try:
            data = {
                "client_id": google.client_id,
                "client_secret": google.client_secret,
                "refresh_token": refresh_token,
                "grant_type": "refresh_token"
            }

            response = requests.post(google.access_token_url, data=data)
            response.raise_for_status()

            token_data = response.json()
            access_token = token_data.get('access_token')

            if not access_token:
                raise ErrorHandler.handle_error(
                    None,
                    message="Failed to obtain a new access token.",
                    status_code=500
                )

            return access_token

        except requests.exceptions.RequestException as e:
            raise RuntimeError("Request failed while fetching google token.") from e
