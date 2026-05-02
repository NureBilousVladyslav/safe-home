import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TablePage from "./tablePage";
import api from "../configs/api";
import Modal from "../components/modal";
import { useTranslation } from "react-i18next";


const Customers = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [refreshKey, setRefreshKey] = useState(0);
  const [notification, setNotification] = useState({ isOpen: false, message: "" });
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, userId: null });

  const columnDefs = [
    { field: "name", headerName: t("customers.name") },
    { field: "email", headerName: t("customers.email") },
    { field: "currentSubscription", headerName: t("customers.currentSubscription") },
    { field: "createdAt", headerName: t("customers.createdAt") },
    {
      field: "goToUser",
      headerName: "",
      cellRenderer: (params) => (
        <button
          className="row-btn go-to-user"
          onClick={() => {
            navigate(`/admin/customers/user/${params.data.user_id}`);
          }}
        >
          {t("customers.goToUser")}
        </button>
      ),
      width: 150,
      filter: false,
      cellStyle: { textAlign: "center" },
    },
    {
      field: "delete",
      headerName: "",
      cellRenderer: (params) => (
        <button
          className="row-btn delete"
          onClick={() => setConfirmModal({ isOpen: true, userId: params.data.user_id })}
        >
          {t("customers.deleteUser")}
        </button>
      ),
      width: 100,
      filter: false,
      cellStyle: { textAlign: "center" },
    },
  ];

  const handleDeleteUser = async (userId) => {
    try {
      const response = await api.delete(`/users/${userId}`);
      if (response.status === 200) {
        setNotification({ isOpen: true, message: t("customers.userDeleted") });
        setRefreshKey((prev) => prev + 1);
      }
    } catch (error) {
      console.error("Error deleting user:", error);
      setNotification({ isOpen: true, message: t("notification.failedToDeleteUser") });
    } finally {
      setConfirmModal({ isOpen: false, userId: null });
    }
  };

  return (
    <>
      <TablePage
        apiEndpoint="/users"
        columnDefs={columnDefs}
        transformData={(data) =>
          data.users.map((user) => ({
            ...user,
            name: user.name,
            email: user.email,
            currentSubscription: user.subscription_plan_name,
            createdAt: new Date(user.created_at).toLocaleDateString(),
          }))
        }
        refreshKey={refreshKey}
      />
      <Modal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false, userId: null })}
        isDialog={true}
        onConfirm={() => handleDeleteUser(confirmModal.userId)}
        confirmText={t("customers.confirm")}
        cancelText={t("customers.cancel")}
      >
        <p>{t("customers.deleteUserConfirmation")}</p>
      </Modal>
      <Modal
        isOpen={notification.isOpen}
        onClose={() => setNotification({ isOpen: false, message: "" })}
        message={notification.message}
        showCloseButton={true}
      />
    </>
  );
};

export default Customers;
