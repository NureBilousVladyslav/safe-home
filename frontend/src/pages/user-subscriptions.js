import { useParams } from "react-router-dom";
import { useState } from "react";
import TablePage from "./tablePage";
import api from "../configs/api";
import Modal from "../components/modal";
import { useTranslation } from "react-i18next";

const UserSubscriptions = () => {
  const { userId } = useParams();
  const { t } = useTranslation();
  const [refreshKey, setRefreshKey] = useState(0);
  const [notification, setNotification] = useState({ isOpen: false, message: "" });

  const columnDefs = [
    { field: "subscriptionPlan", headerName: t("userSubscriptions.subscriptionPlan") },
    { field: "startDate", headerName: t("userSubscriptions.startDate") },
    { field: "endDate", headerName: t("userSubscriptions.endDate") },
    {
      field: "status",
      headerName: t("userSubscriptions.status"),
      cellRenderer: (params) => {
        if (params.data.isActive) {
          return <span style={{ color: "green", fontWeight: "bold" }}>{t("userSubscriptions.active")}</span>;
        } else {
          return <span style={{ color: "red", fontWeight: "bold" }}>{t("userSubscriptions.inactive")}</span>;
        }
      },
      filter: false,
    },
    {
      field: "cancel",
      headerName: "",
      cellRenderer: (params) =>
        params.data.isCancelable ? (
          <button
            className="row-btn cancel"
            onClick={() => handleCancel(params.data.subscriptionId)}
          >
            {t("userSubscriptions.cancel")}
          </button>
        ) : null,
      width: 100,
      filter: false,
      cellStyle: { textAlign: "center" },
    },
  ];

  const transformData = (data) =>
    data.subscriptions.map((sub) => ({
      subscriptionPlan: sub.plan.name,
      startDate: new Date(sub.start_date).toLocaleDateString(),
      endDate: new Date(sub.end_date).toLocaleDateString(),
      isCancelable: sub.is_active && sub.plan.name !== "basic",
      subscriptionId: sub.subscription_id,
      isActive: sub.is_active,
    }));

  const handleCancel = async () => {
    try {
      await api.put(`/subscriptions/current/cancel/${userId}`);
      setNotification({ isOpen: true, message: t("userSubscriptions.subscriptionCanceled") });
      setRefreshKey((prev) => prev + 1);
    } catch (err) {
      console.error("Error canceling subscription:", err);
      setNotification({ isOpen: true, message: t("userSubscriptions.cancelFailed") });
    }
  };

  return (
    <>
      <TablePage
        apiEndpoint={`/subscriptions/${userId}`}
        columnDefs={columnDefs}
        transformData={transformData}
        refreshKey={refreshKey}
      />
      <Modal
        isOpen={notification.isOpen}
        onClose={() => setNotification({ isOpen: false, message: "" })}
        message={notification.message}
        showCloseButton={true}
      />
    </>
  );
};

export default UserSubscriptions;
