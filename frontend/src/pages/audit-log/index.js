import { useState, useEffect, useCallback } from "react";
import { VscRefresh } from "react-icons/vsc";
import { IoMdAddCircle } from "react-icons/io";
import { MdOutlineUpdate } from "react-icons/md";
import { MdRemoveCircle } from "react-icons/md";
import { IoIosArrowDown } from "react-icons/io";
import io from "socket.io-client";
import GradientSpinner from "../../components/gradient-spinner";
import { useTranslation } from "react-i18next";
import "./index.css";

const getIcon = (action) => {
  if (action === "create") return <IoMdAddCircle className="add" />;
  if (action === "update") return <MdOutlineUpdate className="update" />;
  if (action === "delete") return <MdRemoveCircle className="delete" />;
  return <IoMdAddCircle />;
};

const formatTimestamp = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
  const time = date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

  if (diffDays === 0) {
    return `Today at ${time}`;
  } else if (diffDays === 1) {
    return `Yesterday at ${time}`;
  } else if (diffDays < 7) {
    const days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
    return `Last ${days[date.getDay() - 1]} at ${time}`;
  } else {
    const options = { day: "numeric", month: "long" };
    return `${date.toLocaleDateString("en-US", options)} at ${time}`;
  }
};

const AuditLog = () => {
  const { t } = useTranslation();
  const [logs, setLogs] = useState([]);
  const [filteredLogs, setFilteredLogs] = useState([]);
  const [userFilter, setUserFilter] = useState("ALL");
  const [actionFilter, setActionFilter] = useState("ALL");
  const [daysFilter, setDaysFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedLog, setExpandedLog] = useState(null);
  const [socket, setSocket] = useState(null);

  const transformLog = useCallback(
    (log) => ({
      id: log.log_id,
      user: log.admin_name,
      action: log.action,
      timestamp: formatTimestamp(log.created_at),
      actionDetail: log.action_details.action,
      createdAt: new Date(log.created_at),
      actionDetails: log.action_details,
    }),
    []
  );

  useEffect(() => {
    const newSocket = io('https://safe-home-backend-d2f2atb3d0eee9ay.northeurope-01.azurewebsites.net', {
      withCredentials: true,
    });
    setSocket(newSocket);

    newSocket.emit("subscribe_admin_audit_logs", { days: daysFilter !== "ALL" ? parseInt(daysFilter) : null });

    newSocket.on("admin_audit_logs_init", (data) => {
      const transformedLogs = data.admin_audit_logs.map(transformLog);
      setLogs(transformedLogs);
      setFilteredLogs(transformedLogs);
      setLoading(false);
    });

    newSocket.on("admin_audit_log_add", (log) => {
      const transformedLog = transformLog(log);
      setLogs((prevLogs) => [transformedLog, ...prevLogs]);
    });

    newSocket.on("connect_error", (err) => {
      console.error("WebSocket connection error:", err);
      setError(t("auditLog.error"));
      setLoading(false);
    });

    return () => {
      newSocket.disconnect();
    };
  }, [t, transformLog]);

  useEffect(() => {
    let filtered = logs;

    if (userFilter !== "ALL") {
      filtered = filtered.filter((log) => log.user === userFilter);
    }

    if (actionFilter !== "ALL") {
      filtered = filtered.filter((log) => log.actionDetails.action === actionFilter.toLowerCase());
    }

    if (daysFilter !== "ALL") {
      const now = new Date();
      const days = parseInt(daysFilter);
      filtered = filtered.filter((log) => {
        const diffDays = Math.floor((now - log.createdAt) / (1000 * 60 * 60 * 24));
        return diffDays <= days;
      });
    }

    setFilteredLogs(filtered);
  }, [logs, userFilter, actionFilter, daysFilter]);

  useEffect(() => {
    if (socket) {
      socket.emit("subscribe_admin_audit_logs", { days: daysFilter !== "ALL" ? parseInt(daysFilter) : null });
    }
  }, [socket, daysFilter]);

  const handleRefresh = () => {
    if (socket) {
      setLoading(true);
      socket.emit("subscribe_admin_audit_logs", { days: daysFilter !== "ALL" ? parseInt(daysFilter) : null });
    }
  };

  const toggleExpand = (logId) => {
    setExpandedLog(expandedLog === logId ? null : logId);
  };

  const uniqueUsers = ["ALL", ...new Set(logs.map((log) => log.user))];
  const uniqueActions = ["ALL", "create", "update", "delete"];
  const daysOptions = ["ALL", "10", "20", "30", "50", "100"];

  if (loading) {
    return <div className="page loading"><GradientSpinner /></div>;
  }

  if (error) {
    return <div className="page error">{error}</div>;
  }

  return (
    <div className="page audit-log-container">
      <div className="audit-log-header">
        <div className="filter">
          <span>{t("auditLog.admin")}</span>
          <select value={userFilter} onChange={(e) => setUserFilter(e.target.value)}>
            {uniqueUsers.map((user) => (
              <option key={user} value={user}>
                {user}
              </option>
            ))}
          </select>
        </div>
        <div className="filter">
          <span>{t("auditLog.action")}</span>
          <select value={actionFilter} onChange={(e) => setActionFilter(e.target.value)}>
            {uniqueActions.map((action) => (
              <option key={action} value={action}>
                {action}
              </option>
            ))}
          </select>
        </div>
        <div className="filter">
          <span>{t("auditLog.days")}</span>
          <select value={daysFilter} onChange={(e) => setDaysFilter(e.target.value)}>
            {daysOptions.map((days) => (
              <option key={days} value={days}>
                {days === "ALL" ? "ALL" : `${days} days`}
              </option>
            ))}
          </select>
        </div>
        <button className="refresh-btn" onClick={handleRefresh}>
          <VscRefresh className="icon" />
          {t("auditLog.refresh")}
        </button>
      </div>
      <div className="log-list">
        {filteredLogs.map((log) => (
          <div key={log.id} className="log-item">
            <div className="log-item-header" onClick={() => toggleExpand(log.id)}>
              <div className="log-icon">{getIcon(log.actionDetail)}</div>
              <div className="log-details">
                <div className="log-action">
                  <span className="user">{log.user} </span>
                  <span>{log.action} </span>
                </div>
                <span className="log-timestamp">{log.timestamp}</span>
              </div>
              <div className={`log-expand-icon ${expandedLog === log.id ? "expanded" : ""}`}>
                <IoIosArrowDown />
              </div>
            </div>
            {expandedLog === log.id && log.actionDetails && (
              <div className={`log-details-expanded ${log.actionDetails.action}`}>
                {log.actionDetails.details && (
                  <ul>
                    {Object.entries(log.actionDetails.details).map(([key, value], index) => (
                      <li key={index}>
                        - {key}: {value}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default AuditLog;
