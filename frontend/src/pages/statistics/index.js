import "./index.css";
import { useState } from "react";
import { Route, Routes, Link, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { LuUsers } from "react-icons/lu";
import { BiSolidUserAccount } from "react-icons/bi";
import { PiMoneyWavy } from "react-icons/pi";
import UserStatsChart from "../../components/charts/user-count";
import SubscriptionPlansChart from "../../components/charts/plan-chart";
import NotFound from "../not-found";

const Statistics = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const [activeTab, setActiveTab] = useState("user-count");

  const tabs = [
    { id: "user-count", label: t("stats.userCount"), path: "/admin/statistics/user-count", icon: <LuUsers /> },
    { id: "subscription-plans", label: t("stats.subscriptionPlans"), path: "/admin/statistics/subscription-plans", icon: <PiMoneyWavy /> },
    { id: "subscription-plan", label: t("stats.userCountByPlans"), path: "/admin/statistics/user-count-by-plans", icon: <BiSolidUserAccount /> },
  ];

  useState(() => {
    const currentTab = tabs.find((tab) => location.pathname === tab.path)?.id || "user-count";
    setActiveTab(currentTab);
  }, [location.pathname]);

  const handleTabClick = (tabId) => {
    setActiveTab(tabId);
  };

  return (
    <div className="page">
      <div className="stats-navigation">
        {tabs.map((tab) => (
          <Link
            key={tab.id}
            to={tab.path}
            className={`stats-nav-btn ${activeTab === tab.id ? "active" : ""}`}
            onClick={() => handleTabClick(tab.id)}
          >
            <span className="stats-nav-icon">{tab.icon}</span>
            <span>{tab.label}</span>
          </Link>
        ))}
      </div>
      <Routes>
        <Route path="/user-count" element={<UserStatsChart />} />
        <Route path="/subscription-plans" element={<SubscriptionPlansChart />} />
        <Route path="/user-count-by-plans" element={<UserStatsChart multiple={true} />} />
        <Route index element={<UserStatsChart />} />        
        <Route path='*' element={<NotFound />} />
      </Routes>
    </div>
  );
};

export default Statistics;
