import "./index.css";

const GradientSpinner = ({ forForm=false }) => {
  return (
    <div className={`spinner-container ${forForm ? 'form-spinner' : ''}`}>
      <svg className="spinner" viewBox="0 0 100 100">
        <path
          d="M 50 10 A 40 40 0 0 1 90 50"
          fill="none"
          stroke="url(#gradient)"
          strokeWidth="8"
          strokeLinecap="round"
          className="spinner-path"
        />
        <defs>
          <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style={{ stopColor: "#E6F0FA" }} />
            <stop offset="25%" style={{ stopColor: "#A3BFFA" }} />
            <stop offset="50%" style={{ stopColor: "#4C6EF5" }} />
            <stop offset="75%" style={{ stopColor: "#1E3A8A" }} />
            <stop offset="100%" style={{ stopColor: "#FFFFFF" }} />
          </linearGradient>
        </defs>
      </svg>
    </div>
  );
};

export default GradientSpinner;