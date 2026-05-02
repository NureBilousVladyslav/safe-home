import './index.css';
import logo from './logo.png';
import { FiUsers } from "react-icons/fi";
import { GrUserAdmin } from "react-icons/gr";
import { MdPayment } from "react-icons/md";
import { RxReader } from "react-icons/rx";
import { IoStatsChart } from "react-icons/io5";
import { IoArrowBackOutline } from "react-icons/io5";
import { RxHamburgerMenu } from "react-icons/rx";
import { useState, useEffect } from 'react';
import { Link, useLocation } from "react-router-dom";
import { useTranslation } from 'react-i18next';
import UserPanel from '../user-panel';
import api from '../../configs/api';

const Navigation = ({ changeLanguage }) => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [userEmail, setUserEmail] = useState(null);
  const location = useLocation();
  const userId = location.pathname.split('/')[4];
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const navLinks = [
    { path: '/admin/customers', icon: <FiUsers className='icon' />, label: t('navigation.customers') },
    { path: '/admin/admins', icon: <GrUserAdmin className='icon' />, label: t('navigation.admins') },
    { path: '/admin/subscriptions', icon: <MdPayment className='icon' />, label: t('navigation.subscriptions') },
    { path: '/admin/audit-log', icon: <RxReader className='icon' />, label: t('navigation.auditLog') },
    { path: '/admin/statistics', icon: <IoStatsChart className='icon' />, label: t('navigation.statistics') },
  ];

  useEffect(() => {
    if (t('navigation.customers')) {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    if (!location.pathname.startsWith("/admin/customers/user")) {
      setUserEmail(null);
    }
  }, [location.pathname]);

  useEffect(() => {
    const fetchUserEmail = async () => {
      try {
        const response = await api.get(`/users/${userId}`);
        if (response.data && response.data.user.email) {
          setUserEmail(response.data.user.email);
        }
      } catch (error) {
        console.log(error);
      }
    };
    if (userId) {
      fetchUserEmail();
    }
  }, [userId]);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const getActiveLink = (path) => {
    return location.pathname.startsWith(path) ? 'active' : '';
  };

  const getPageTitle = () => {
    if (userEmail) {
      return userEmail;
    }
    if (getActiveLink('/admin/customers') === 'active') return t('navigation.customers');
    if (getActiveLink('/admin/admins') === 'active') return t('navigation.admins');
    if (getActiveLink('/admin/subscriptions') === 'active') return t('navigation.subscriptions');
    if (getActiveLink('/admin/audit-log') === 'active') return t('navigation.auditLog');
    if (getActiveLink('/admin/statistics') === 'active') return t('navigation.statistics');
    return 'safe home';
  };

  const handleLinkClick = () => {
    if (window.innerWidth <= 768) {
      toggleMenu();
    }
  };

  if (loading) {
    return null;
  }

  return (
    <div className="navigation-wrapper">
      <div className={`navigation ${isMenuOpen ? 'expanded' : ''}`}>
        <div className='logo'>
          <RxHamburgerMenu className="burger-menu-icon" onClick={toggleMenu} />
          <span className="mobile-title">{getPageTitle()}</span>
        </div>
        <div className="overlay" onClick={toggleMenu}></div>
        <div className='navigation-container'>
          <Link to={'/'} className='mobile-title'>
            <img src={logo} alt='company-logo' className="desktop-logo" />
            <span className="desktop-text">safe home</span>
          </Link>
          <div className='links'>
            {navLinks.map(({ path, icon, label }) => (
              <Link
                key={path}
                to={path}
                className={`link ${getActiveLink(path)} ${userEmail && path === '/admin/customers' ? 'email' : ''}`}
                onClick={handleLinkClick}
              >
                <div className='customer-link'>
                  {userEmail && path === '/admin/customers' ? (
                    <IoArrowBackOutline className='icon arrow' />
                  ) : (
                    icon
                  )}
                  {label}
                </div>
                {userEmail && path === '/admin/customers' && (
                  <div className="user-email">
                    {userEmail}
                  </div>
                )}
              </Link>
            ))}
          </div>
          <UserPanel changeLanguage={changeLanguage} />
        </div>
      </div>
    </div>
  );
};

export default Navigation;
