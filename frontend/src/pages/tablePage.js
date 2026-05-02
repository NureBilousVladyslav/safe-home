import { useState, useEffect, useCallback } from "react";
import { AgGridReact } from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry, themeMaterial } from "ag-grid-community";
import "ag-grid-community/styles/ag-theme-material.css";
import api from "../configs/api";
import "./tablePage.css";
import { IoAdd } from "react-icons/io5";
import { useTranslation } from "react-i18next";
import { useMediaQuery } from "react-responsive";
import Card from "../components/card";
import GradientSpinner from "../components/gradient-spinner";

ModuleRegistry.registerModules([AllCommunityModule]);

const TablePage = ({ apiEndpoint, columnDefs, transformData,
  showActions = false, onRowClicked = null, onAddClick, refreshKey }) => {
  const { t } = useTranslation();
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const isMobile = useMediaQuery({ maxWidth: 768 });

  const fetchData = useCallback(async () => {
    try {
      const response = await api.get(apiEndpoint);
      const data = transformData ? transformData(response.data) : response.data;
      setRowData(data);
    } catch (err) {
      console.error("Error fetching data:", err);
      setError(t("tablePage.error"));
    } finally {
      setLoading(false);
    }
  }, [apiEndpoint, transformData, t]);

  useEffect(() => {
    fetchData();
  }, [fetchData, refreshKey]);

  if (loading) {
    return <div className="page loading">
      <GradientSpinner />
    </div>;
  }

  if (error) {
    return <div className="page error">{error}</div>;
  }

  return (
    <div className="page">
      {isMobile ? (
        <div className="card-list">
          {rowData.map((row, index) => (
            <Card key={index} data={row} columns={columnDefs} onClick={onRowClicked} />
          ))}
        </div>
      ) : (
        <div className="table ag-theme-material" style={{ height: "70%", width: "100%" }}>
          <AgGridReact
            rowData={rowData}
            columnDefs={columnDefs}
            rowSelection="multiple"
            pagination={true}
            paginationPageSize={20}
            paginationPageSizeSelector={[10, 20, 30, 40]}
            defaultColDef={{ filter: true, flex: 1 }}
            onRowClicked={onRowClicked}
            theme={themeMaterial}
          />
        </div>
      )}
      {showActions && (
        <div className="actions">
          <button className="row-btn add" onClick={onAddClick}>
            <IoAdd className="icon" /> {t("tablePage.addNew")}
          </button>
        </div>
      )}
    </div>
  );
};

export default TablePage;
