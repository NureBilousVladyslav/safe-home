import { useState } from "react";
import TablePage from "./tablePage";
import RegisterForm from "../components/forms/register";
import Modal from "../components/modal";
import { useTranslation } from "react-i18next";
import { useAuth } from "../contexts/auth-context";
import api from "../configs/api";

const Admins = () => {
  const { t } = useTranslation();
  const [modalIsOpen, setModalIsOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [notification, setNotification] = useState({ isOpen: false, message: "" });
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, adminId: null });
  const { userData } = useAuth();

  const columnDefs = [
    { field: "name", headerName: t("admins.name") },
    { field: "email", headerName: t("admins.email") },
    { field: "createdAt", headerName: t("admins.createdAt") },
  ];

  if (userData?.role === "super_admin") {
    columnDefs.push({
      field: "delete",
      headerName: "",
      cellRenderer: (params) => (
        <button
          className="row-btn delete"
          onClick={() => setConfirmModal({ isOpen: true, adminId: params.data.user_id })}
        >
          {t("admins.deleteAdmin")}
        </button>
      ),
      width: 100,
      filter: false,
      cellStyle: { textAlign: "center" },
    });
  }

  const transformData = (data) =>
    data.admins.map((admin) => ({
      ...admin,
      name: admin.name,
      email: admin.email,
      createdAt: new Date(admin.created_at).toLocaleDateString(),
    }));

  const handleModalClose = (shouldRefresh = false) => {
    setModalIsOpen(false);
    if (shouldRefresh) {
      setRefreshKey((prev) => prev + 1);
    }
  };

  const handleDeleteAdmin = async (adminId) => {
    try {
      const response = await api.delete(`/admins/${adminId}`);
      if (response.status === 200) {
        setNotification({ isOpen: true, message: t("admins.adminDeleted") });
        setRefreshKey((prev) => prev + 1);
      }
    } catch (error) {
      console.error("Error deleting admin:", error);
      setNotification({ isOpen: true, message: t("notification.failedToDeleteAdmin") });
    } finally {
      setConfirmModal({ isOpen: false, adminId: null });
    }
  };

  return (
    <>
      <TablePage
        apiEndpoint="/admins"
        columnDefs={columnDefs}
        transformData={transformData}
        showActions={true}
        onAddClick={() => setModalIsOpen(true)}
        refreshKey={refreshKey}
      />
      {modalIsOpen && (
        <Modal isOpen={modalIsOpen} onClose={() => handleModalClose(false)}>
          <RegisterForm
            onBack={() => handleModalClose(false)}
            onSuccess={() => handleModalClose(true)}
          />
        </Modal>
      )}
      <Modal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false, adminId: null })}
        isDialog={true}
        onConfirm={() => handleDeleteAdmin(confirmModal.adminId)}
        confirmText={t("admins.confirm")}
        cancelText={t("admins.cancel")}
      >
        <p>{t("admins.deleteAdminConfirmation")}</p>
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

export default Admins;
