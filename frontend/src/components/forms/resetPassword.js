import { useTranslation } from "react-i18next";
import FormTemplate from "./template";
import api from "../../configs/api";

export const ResetPasswordForm = ({ onBack, changeLanguage }) => {
  const { t } = useTranslation();

  const handleResetPassword = async (data, setStatus) => {
    try {
      const response = await api.post("/reset-password", { email: data.email });
      if (response.status === 200) {
        setStatus({
          message: t("resetPassword.success"),
          type: "success",
        });
        setTimeout(() => {
          onBack();
        }, 3000);
      }
    } catch (error) {
      setStatus({
        message: error.response?.data?.message || t("resetPassword.failed"),
        type: "error",
      });
    }
  };

  return (
    <FormTemplate
      title={t("resetPassword.title")}
      buttonText={t("resetPassword.buttonText")}
      onSubmit={handleResetPassword}
      fields={[
        {
          name: "email",
          type: "email",
          placeholder: t("resetPassword.emailPlaceholder"),
          validation: { required: t("resetPassword.emailRequired") },
        },
      ]}
      onBack={onBack}
      className={"reset-password-form"}
      changeLanguage={changeLanguage}
      isResetPassword={true}
    />
  );
};
