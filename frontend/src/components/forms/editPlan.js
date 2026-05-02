import { useTranslation } from "react-i18next";
import FormTemplate from "./template";
import api from "../../configs/api";

export const EditPlanForm = ({ initialData = null, onBack, onSuccess }) => {
  const { t } = useTranslation();

  const handleSubmit = async (data, setStatus) => {
    try {
      const formattedData = {
        name: data.name,
        max_homes: parseInt(data.maxHomes, 10),
        max_sensors: parseInt(data.maxSensors, 10),
        price: parseFloat(data.price),
        duration_days: parseInt(data.duration, 10),
      };

      if (initialData) {
        await api.patch(`/subscription-plans/${initialData.id}`, formattedData);
        setStatus({ message: t("editPlanForm.planUpdatedSuccessfully"), type: "success" });
      } else {
        await api.post("/subscription-plans", formattedData);
        setStatus({ message: t("editPlanForm.planAddedSuccessfully"), type: "success" });
      }

      setTimeout(() => {
        onSuccess();
        onBack();
      }, 1000);
    } catch (error) {
      setStatus({
        message: error.response?.data?.message || t("editPlanForm.failedToSavePlan"),
        type: "error",
      });
    }
  };

  return (
    <FormTemplate
      title={initialData ? t("editPlanForm.editPlan") : t("editPlanForm.addNewPlan")}
      buttonText={initialData ? t("editPlanForm.saveChanges") : t("editPlanForm.addPlan")}
      onSubmit={handleSubmit}
      fields={[
        {
          name: "name",
          type: "text",
          label: t("editPlanForm.name"),
          defaultValue: initialData?.name || "",
          validation: { required: t("editPlanForm.nameRequired") },
          showLabel: true,
        },
        {
          name: "maxHomes",
          type: "number",
          label: t("editPlanForm.maxHomes"),
          defaultValue: initialData?.maxHomes || "",
          showLabel: true,
        },
        {
          name: "maxSensors",
          type: "number",
          label: t("editPlanForm.maxSensors"),
          defaultValue: initialData?.maxSensors || "",
          showLabel: true,
        },
        {
          name: "price",
          type: "text",
          label: t("editPlanForm.price"),
          defaultValue: initialData?.price || "",
          validation: {
            required: t("editPlanForm.priceRequired"),
            pattern: {
              value: /^\d+(\.\d{1,2})?$/,
              message: t("editPlanForm.pricePattern"),
            },
          },
          showLabel: true,
        },
        {
          name: "duration",
          type: "number",
          label: t("editPlanForm.duration"),
          defaultValue: initialData?.duration || "",
          showLabel: true,
        },
      ]}
      onBack={onBack}
    />
  );
};
