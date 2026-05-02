import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../contexts/auth-context';
import { IoIosArrowDown } from "react-icons/io";
import { MdLogout } from "react-icons/md";
import { FiEye, FiEyeOff } from "react-icons/fi";
import { useForm } from "react-hook-form";
import LanguageSwitcher from '../language-switcher';
import Modal from '../modal';
import api from '../../configs/api';
import './index.css';

const UserPanel = ({ changeLanguage }) => {
  const { t } = useTranslation();
  const { logout, userData } = useAuth();
  const [isAccordionOpen, setIsAccordionOpen] = useState(false);
  const [notification, setNotification] = useState({ isOpen: false, message: "" });
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const { register, handleSubmit, formState: { errors }, reset } = useForm();

  const handleLogout = () => {
    logout();
  };

  const toggleAccordion = () => {
    setIsAccordionOpen(!isAccordionOpen);
  };

  const onSubmit = async (data) => {
    try {
      const response = await api.put('/user/password', {
        old_password: data.oldPassword,
        new_password: data.newPassword,
      });
      if (response.status === 200) {
        setNotification({ isOpen: true, message: t("notification.passwordUpdated") });
        setTimeout(() => {
          toggleAccordion();
          reset();
        }, 700);
      }
    } catch (error) {
      setNotification({ isOpen: true, message: t("notification.passwordUpdateFailed") });
    }
  };

  return (
    <div className='user-panel'>
      <div className='user-header'>
        <p className='name'>{userData?.name || ''}</p>
        <p className='email'>{userData?.email || ''}</p>
        <IoIosArrowDown
          className={`icon ${isAccordionOpen ? 'open' : ''}`}
          onClick={toggleAccordion}
        />
        {isAccordionOpen && (
          <div className={`accordion-content ${isAccordionOpen ? 'open' : ''}`}>
            <form className='form' onSubmit={handleSubmit(onSubmit)}>
              <div className='form-group'>
                <input
                  type={showOldPassword ? 'text' : 'password'}
                  {...register('oldPassword', {
                    required: t('login.passwordRequired'),
                    minLength: { value: 8, message: t('login.passwordMinLength', { length: 8 }) },
                  })}
                  placeholder={t('navigation.oldPassword')}
                />
                <button
                  type="button"
                  className="icon"
                  onClick={() => setShowOldPassword(!showOldPassword)}
                >
                  {showOldPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
              {errors.oldPassword && <p className="error">{errors.oldPassword.message}</p>}
              <div className='form-group'>
                <input
                  type={showNewPassword ? 'text' : 'password'}
                  {...register('newPassword', {
                    required: t('login.passwordRequired'),
                    minLength: { value: 8, message: t('login.passwordMinLength', { length: 8 }) },
                  })}
                  placeholder={t('navigation.newPassword')}
                />
                <button
                  type="button"
                  className="icon"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                >
                  {showNewPassword ? <FiEyeOff /> : <FiEye />}
                </button>
              </div>
              {errors.newPassword && <p className="error">{errors.newPassword.message}</p>}
              <button type='submit'>{t('navigation.changePassword')}</button>
            </form>
            <LanguageSwitcher changeLanguage={changeLanguage} />
          </div>
        )}
      </div>
      <button className='logout' onClick={handleLogout}>
        <MdLogout className='icon' />
        <span>{t('navigation.logout')}</span>
      </button>
      <Modal
        isOpen={notification.isOpen}
        onClose={() => setNotification({ isOpen: false, message: "" })}
        message={notification.message}
        showCloseButton={true}
      />
    </div>
  );
};

export default UserPanel;
