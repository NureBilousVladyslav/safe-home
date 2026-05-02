import { useState, useEffect } from "react";
import { Bar } from "react-chartjs-2";
import { Chart as ChartJS, BarElement, CategoryScale, LinearScale, Title, Tooltip, Legend } from "chart.js";
import api from "../../../configs/api";
import GradientSpinner from '../../gradient-spinner';
import { useTranslation } from "react-i18next";
import './index.css';

ChartJS.register(BarElement, CategoryScale, LinearScale, Title, Tooltip, Legend);

const SubscriptionPlansChart = () => {
  const { t } = useTranslation();
  const [chartData, setChartData] = useState({ users: null, homes: null, sensors: null });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await api.get("/stats/subscription-plans");
        const stats = response.data.subscription_plans_stats;

        if (!stats || stats.length === 0) {
          setChartData({ users: null, homes: null, sensors: null });
          return;
        }

        const labels = stats.map((stat) => stat.plan_name);
        const userCountData = stats.map((stat) => stat.user_count);
        const avgHomesData = stats.map((stat) => stat.avg_homes);
        const planMaxHomesData = stats.map((stat) => stat.plan_max_homes || 0);
        const avgSensorsData = stats.map((stat) => stat.avg_sensors);
        const planMaxSensorsData = stats.map((stat) => stat.plan_max_sensors || 0);

        setChartData({
          users: {
            labels,
            datasets: [
              {
                label: t("charts.userCount"),
                data: userCountData,
                backgroundColor: "rgba(108, 122, 231, 0.6)",
                borderColor: "rgb(108, 122, 231)",
                borderWidth: 1,
              },
            ],
          },
          homes: {
            labels,
            datasets: [
              {
                label: t("charts.avgHomes"),
                data: avgHomesData,
                backgroundColor: "rgba(255, 99, 132, 0.6)",
                borderColor: "rgb(255, 99, 132)",
                borderWidth: 1,
              },
              {
                label: t("charts.planMaxHomes"),
                data: planMaxHomesData,
                backgroundColor: "rgba(255, 159, 64, 0.6)",
                borderColor: "rgb(255, 159, 64)",
                borderWidth: 1,
              },
            ],
          },
          sensors: {
            labels,
            datasets: [
              {
                label: t("charts.avgSensors"),
                data: avgSensorsData,
                backgroundColor: "rgba(54, 162, 235, 0.6)",
                borderColor: "rgb(54, 162, 235)",
                borderWidth: 1,
              },
              {
                label: t("charts.planMaxSensors"),
                data: planMaxSensorsData,
                backgroundColor: "rgba(75, 192, 192, 0.6)",
                borderColor: "rgb(75, 192, 192)",
                borderWidth: 1,
              },
            ],
          },
        });
      } catch (error) {
        console.error("Error fetching subscription plans stats:", error);
        setChartData({ users: null, homes: null, sensors: null });
      }
    };

    fetchStats();
  }, [t]);

  if (!chartData.users || !chartData.homes || !chartData.sensors) return <div className="page loading"><GradientSpinner /></div>;

  return (
    <div className="subsctription-chart">
      <h3>{t("charts.userCountTitle")}</h3>
      <Bar
        className="chart"
        data={chartData.users}
        options={{
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: t("charts.planNameAxis"),
              },
            },
            y: {
              beginAtZero: true,
              ticks: {
                stepSize: 1,
              },
              title: {
                display: true,
                text: t("charts.userCountAxis"),
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
          },
        }}
      />
      <h3>{t("charts.homesComparisonTitle")}</h3>
      <Bar
        className="chart"
        data={chartData.homes}
        options={{
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: t("charts.planNameAxis"),
              },
            },
            y: {
              beginAtZero: true,
              ticks: {
                stepSize: 1,
              },
              title: {
                display: true,
                text: t("charts.homesAxis"),
              },
            },
          },
          plugins: {
            legend: {
              position: 'top',
            },
          },
        }}
      />
      <h3>{t("charts.sensorsComparisonTitle")}</h3>
      <Bar
        className="chart"
        data={chartData.sensors}
        options={{
          responsive: true,
          scales: {
            x: {
              title: {
                display: true,
                text: t("charts.planNameAxis"),
              },
            },
            y: {
              beginAtZero: true,
              ticks: {
                stepSize: 1,
              },
              title: {
                display: true,
                text: t("charts.sensorsAxis"),
              },
            },
          },
          plugins: {
            legend: {
              position: 'top',
            },
          },
        }}
      />
    </div>
  );
};

export default SubscriptionPlansChart;
