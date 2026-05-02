import { useState, useEffect } from "react";
import { Line } from "react-chartjs-2";
import { Chart as ChartJS, LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend } from "chart.js";
import "chartjs-adapter-date-fns";
import api from "../../../configs/api";
import GradientSpinner from "../../gradient-spinner";
import { useTranslation } from "react-i18next";
import './index.css';

ChartJS.register(LineElement, PointElement, LinearScale, TimeScale, Title, Tooltip, Legend);

const UserStatsChart = ({ multiple = false }) => {
  const { t } = useTranslation();
  const [chartData, setChartData] = useState(null);
  const [days, setDays] = useState(7);

  const daysRange = {
    min: 1,
    max: 200,
  };

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = multiple ? await api.get(`/stats/subscription-plan/all/${days}`) : await api.get(`/stats/users/${days}`);
        const stats = multiple ? response.data.subscription_plans_stats : response.data.user_stats;

        if (multiple) {
          if (!stats || stats.length === 0) {
            setChartData(null);
            return;
          }

          const colors = [
            { border: "rgb(108, 122, 231)", background: "rgba(108, 122, 231, 0.2)" },
            { border: "rgb(255, 99, 132)", background: "rgba(255, 99, 132, 0.2)" },
            { border: "rgb(54, 162, 235)", background: "rgba(54, 162, 235, 0.2)" },
            { border: "rgb(75, 192, 192)", background: "rgba(75, 192, 192, 0.2)" },
          ];

          const datasets = stats.map((plan, index) => {
            const planStats = plan.stats;
            const planColor = colors[index % colors.length];
            return {
              label: plan.plan_name,
              data: planStats.map((stat) => ({ x: new Date(stat.date), y: stat.user_count })),
              borderColor: planColor.border,
              backgroundColor: planColor.background,
              fill: true,
              tension: 0.3,
            };
          });

          setChartData({
            datasets,
          });
        } else {
          const labels = stats.map((stat) => new Date(stat.date));
          const data = stats.map((stat) => stat.user_count);

          setChartData({
            labels,
            datasets: [
              {
                label: t("charts.userCount"),
                data,
                borderColor: "rgb(108, 122, 231)",
                backgroundColor: "rgba(108, 122, 231, 0.2)",
                fill: true,
                tension: 0.3,
              },
            ],
          });
        }
      } catch (error) {
        console.error("Error fetching user stats:", error);
        setChartData(null);
      }
    };

    fetchStats();
  }, [days, t, multiple]);

  const handleDaysChange = (e) => {
    const value = parseInt(e.target.value);
    if (value > 0) {
      setDays(value);
    }
  };

  if (!chartData) return <div className="page loading"><GradientSpinner /></div>;

  return (
    <div className="user-count-chart">
      <Line
        className="chart"
        data={chartData}
        options={{
          responsive: true,
          scales: {
            x: {
              type: "time",
              time: {
                unit: days <= daysRange.min ? "hour" : days <= daysRange.max ? "day" : "month",
                displayFormats: {
                  hour: "HH:00",
                  day: "MMM dd",
                  month: "MMM yyyy",
                },
                auto: true,
              },
              title: {
                display: true,
                text: t("charts.dateAxis"),
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
              display: multiple,
              position: 'top',
            },
            tooltip: {
              enabled: true,
              mode: 'nearest',
              intersect: false,
              callbacks: {
                label: (context) => {
                  const label = context.dataset.label || '';
                  const value = context.parsed.y;
                  return `${label}: ${value}`;
                },
                title: (tooltipItems) => {
                  const date = new Date(tooltipItems[0].parsed.x);
                  return date.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  });
                },
              },
            },
          },
          interaction: {
            mode: 'nearest',
            intersect: false,
            axis: 'x',
          },
        }}
      />
      <div className="action-panel">
        <label>{t("charts.daysLabel")}</label>
        <input
          type="range"
          value={days}
          onChange={handleDaysChange}
          min={daysRange.min.toString()}
          max={daysRange.max.toString()}
        />
        <input
          type="number"
          value={days}
          onChange={handleDaysChange}
          min={daysRange.min.toString()}
          max={daysRange.max.toString()}
        />
      </div>
    </div>
  );
};

export default UserStatsChart;
