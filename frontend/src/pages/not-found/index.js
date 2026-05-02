import './index.css';
import { useTranslation } from 'react-i18next';
import { TbError404 } from "react-icons/tb";
import { FaArrowLeft } from "react-icons/fa6";

const NotFound = () => {
  const { t } = useTranslation();

  const handleClick = () => {
    window.location.href = '/';
  };

  return (
    <div className='page not-found'>
      <TbError404 className='icon' />
      <p className='title'>{t("notFound.title")}</p>
      <button className='go-back' onClick={handleClick}>
        <FaArrowLeft />
        {t("notFound.goBack")}
      </button>
    </div>
  );
};

export default NotFound;