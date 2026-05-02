import FormTemplate from "./template";
import api from "../../configs/api";
import { useTranslation } from "react-i18next";

const RegisterForm = ({ onBack, onSuccess }) => {
  const { t } = useTranslation();

  const handleRegister = async (data, setStatus) => {
    try {
      const response = await api.post("/register/admin", data);
      if (response.status === 201) {
        setStatus({ message: t("registerForm.adminAddedSuccessfully"), type: "success" });
        setTimeout(() => {
          onSuccess();
          onBack();
        }, 1000);
      }
    } catch (error) {
      setStatus({
        message: error.response?.data?.message || t("registerForm.failedToRegisterAdmin"),
        type: "error",
      });
    }
  };

  return (
    <FormTemplate
      title={t("registerForm.addNewAdmin")}
      buttonText={t("registerForm.register")}
      onSubmit={handleRegister}
      fields={[
        {
          name: "name",
          type: "text",
          placeholder: t("registerForm.name"),
          validation: { required: t("registerForm.nameRequired") },
        },
        {
          name: "email",
          type: "email",
          placeholder: t("registerForm.email"),
          validation: { required: t("registerForm.emailRequired") },
        },
        {
          name: "password",
          type: "password",
          placeholder: t("registerForm.password"),
          validation: {
            required: t("registerForm.passwordRequired"),
            minLength: { value: 8, message: t("registerForm.passwordMinLength") },
          },
        },
      ]}
      onBack={onBack}
    />
  );
};

export default RegisterForm;
 