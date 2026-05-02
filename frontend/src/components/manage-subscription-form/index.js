import './index.css';
import logo from '../navigation/logo.png';
import { useAuth } from '../../contexts/auth-context';
import { BiCoinStack } from "react-icons/bi";
import { BiDollarCircle } from "react-icons/bi";
import { MdOutlineSensors } from "react-icons/md";
import { FiHome } from "react-icons/fi";
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../../configs/api';
import Modal from '../modal';

const ManageSubscriptionForm = () => {
  const { logout } = useAuth();  
  const [currentSubscription, setCurrentSubscription] = useState(null);
  const [plans, setPlans] = useState([]);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false });
  const [notification, setNotification] = useState({ isOpen: false, message: "" });
  const [refresh, setRefresh] = useState(false);

  useEffect(() => {
    const fetchCurrentSubscription = async () => {
      try {
        const response = await api.get('/subscriptions/current');
        if (response.status === 200) {
          setCurrentSubscription(response.data);
        }
      } catch (error) {
        console.error('Error fetching subscription:', error);
      }
    };

    const fetchPlans = async () => {
      try {
        const response = await api.get('/subscription-plans');
        if (response.status === 200) {
          setPlans(response.data.subscription_plans);
        }
      } catch (error) {
        console.error('Error fetching plans:', error);
      }
    };

    fetchCurrentSubscription();
    fetchPlans();
  }, [refresh]);

  const handleCancelCurrentSubscription = async () => {
    try {
      const response = await api.post('/subscriptions/current/cancel');
      if (response.status === 200) {
        setCurrentSubscription(null);
        setRefresh(!refresh);
        setNotification({ isOpen: true, message: "Subscription canceled successfully" });
      }
    } catch (error) {
      console.error('Error canceling subscription:', error);
      setNotification({ isOpen: true, message: "Failed to cancel subscription" });
    } finally {
      setConfirmModal({ isOpen: false });
    }
  };

  const handleLogout = () => {
    logout();
  };

  const getButtonTextAndStyle = (plan) => {
    if (plan.name === 'basic') {
      return { text: 'Free', className: 'subscribe disabled', disabled: true };
    }
    if (currentSubscription?.plan.name === plan.name && plan.name !== 'basic') {
      return { text: `${plan.price}$ / Extend`, className: 'subscribe active blue', disabled: false };
    }
    return { text: `${plan.price}$ / Buy`, className: 'subscribe', disabled: false };
  };

  const handleCreateCheckoutSession = async (planId) => {
    try {
      const response = await api.post(`/payments/create-checkout-session/${planId}`);
      if (response.status === 200) {
        window.location.href = response.data.url;
      }
    } catch (error) {
      console.error('Error creating checkout session:', error);
      setNotification({ isOpen: true, message: "Failed to initiate payment" });
    }
  };

  const handlePlanAction = (plan) => {
    if (currentSubscription?.plan.name !== 'basic' && plan.name !== currentSubscription?.plan.name) {
      setNotification({ isOpen: true, message: "First cancel your paid subscription" });
    } else if (plan.name !== 'basic') {
      handleCreateCheckoutSession(plan.plan_id);
    }
  };

  return (
    <div className="sub-form">
      <div className="header">
        <Link className='logo-container' to={'/'}>
          <img src={logo} alt="Logo" className="logo" />
          <h3>Safe home</h3>
        </Link>
        <button className='logout-button' onClick={handleLogout}>
          Logout
        </button>
      </div>
      <div className='active-sub'>
        <h2>Your active subscription</h2>
        <div className='info-container'>
          <span className='icon'>
            <BiCoinStack />
            <BiDollarCircle className='dollar' />
          </span>
          <div className='info'>
            {currentSubscription && (
              <>
                <p className='plan'>{currentSubscription.plan.name}</p>
                <p className='starts'>Started: {new Date(currentSubscription.start_date).toLocaleDateString()}</p>
                <p className='ends'>Ends: {new Date(currentSubscription.end_date).toLocaleDateString()}</p>
              </>
            )}
          </div>
          <button className={`cancel ${currentSubscription?.plan.name === 'basic' ? 'hidden' : ''}`}
            onClick={() => setConfirmModal({ isOpen: true })}
            disabled={currentSubscription?.plan.name === 'basic'}>
            Cancel
          </button>
        </div>
      </div>
      <div className='plans-list'>
        <h2>Plans</h2>
        <div className='container'>
          {plans.map((plan) => {
            const sensorText = plan.max_sensors === 1 ? 'Sensor' : 'Sensors';
            const homeText = plan.max_homes === 1 ? 'Home' : 'Homes';
            const { text, className, disabled } = getButtonTextAndStyle(plan);

            return (
              <div className='item' key={plan.plan_id}>
                <div className='info'>
                  <h3 className='name'>{plan.name}</h3>
                  <div>
                    <MdOutlineSensors className='icon' />
                    <span>{plan.max_sensors} {sensorText}</span>
                  </div>
                  <div>
                    <FiHome className='icon' />
                    <span>{plan.max_homes} {homeText}</span>
                  </div>
                </div>
                <h3 className='duration'>{plan.duration} Days</h3>
                <button
                  className={className}
                  disabled={disabled}
                  onClick={() => !disabled && handlePlanAction(plan)}
                >
                  {text}
                </button>
              </div>
            );
          })}
        </div>
      </div>
      <Modal
        isOpen={confirmModal.isOpen}
        onClose={() => setConfirmModal({ isOpen: false })}
        isDialog={true}
        onConfirm={handleCancelCurrentSubscription}
        confirmText="Confirm"
        cancelText="Cancel"
      >
        <p>Are you sure you want to cancel your subscription?</p>
      </Modal>
      <Modal
        isOpen={notification.isOpen}
        onClose={() => setNotification({ isOpen: false, message: "" })}
        message={notification.message}
        showCloseButton={true}
      />
    </div>
  );
};

export default ManageSubscriptionForm;
