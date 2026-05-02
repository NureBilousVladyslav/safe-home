import { useState } from "react";
import TablePage from "./tablePage";
import Modal from "../components/modal";
import { EditPlanForm } from "../components/forms/editPlan";
import { useTranslation } from "react-i18next";

const Subscriptions = () => {
  const { t } = useTranslation();
  const [modalIsOpen, setModalIsOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [editData, setEditData] = useState(null);

  const columnDefs = [
    { field: "name", headerName: t("subscriptions.name") },
    { field: "maxHomes", headerName: t("subscriptions.maxHomes") },
    { field: "maxSensors", headerName: t("subscriptions.maxSensors") },
    { field: "price", headerName: t("subscriptions.price") },
    { field: "duration", headerName: t("subscriptions.duration") },
    {
      field: "edit",
      headerName: "",
      cellRenderer: (params) => (
        <button
          className="row-btn edit"
          onClick={() => {
            setEditData(params.data);
            setModalIsOpen(true);
          }}
        >
          {t("subscriptions.edit")}
        </button>
      ),
      width: 100,
      filter: false,
      cellStyle: { textAlign: "center" },
    },
  ];

  const transformData = (data) =>
    data.subscription_plans.map((plan) => ({
      id: plan.plan_id,
      name: plan.name,
      maxHomes: plan.max_homes,
      maxSensors: plan.max_sensors,
      price: plan.price.toFixed(1),
      duration: plan.duration,
    }));

  const handleModalClose = (shouldRefresh = false) => {
    setModalIsOpen(false);
    setEditData(null);
    if (shouldRefresh) {
      setRefreshKey((prev) => prev + 1);
    }
  };

  return (
    <>
      <TablePage
        apiEndpoint="/subscription-plans"
        columnDefs={columnDefs}
        transformData={transformData}
        showActions={true}
        onAddClick={() => {
          setEditData(null);
          setModalIsOpen(true);
        }}
        refreshKey={refreshKey}
      />
      <Modal isOpen={modalIsOpen} onClose={() => handleModalClose(false)}>
        <EditPlanForm
          initialData={editData}
          onBack={() => handleModalClose(false)}
          onSuccess={() => handleModalClose(true)}
        />
      </Modal>
    </>
  );
};

export default Subscriptions;
