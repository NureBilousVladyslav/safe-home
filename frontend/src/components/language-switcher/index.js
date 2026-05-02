import { useTranslation } from 'react-i18next';
import Flag from 'react-world-flags';
import './index.css';

const LanguageSwitcher = ({ changeLanguage }) => {
  const { i18n } = useTranslation();

  return (
    <div className="language-switcher">
      <button
        className={`button ${i18n.language === "en" ? "active" : ""}`}
        onClick={() => changeLanguage('en')}
      >
        <Flag code="US" className="flag" alt="English" />
      </button>
      <button
        className={`button ${i18n.language === "ua" ? "active" : ""}`}
        onClick={() => changeLanguage('ua')}
      >
        <Flag code="UA" className="flag" alt="Ukrainian" />
      </button>
    </div>
  );
};

export default LanguageSwitcher;