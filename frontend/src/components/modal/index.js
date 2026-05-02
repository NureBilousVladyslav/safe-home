import ReactDOM from "react-dom";
import { useEffect } from "react";
import "./index.css";
import { IoCloseOutline } from "react-icons/io5";

const Modal = ({
  isOpen,
  onClose,
  children,
  message = null,
  showCloseButton = false,
  autoCloseTime = 7000,
  isDialog = false,
  onConfirm = null,
  confirmText = "Confirm",
  cancelText = "Cancel"
}) => {
  useEffect(() => {
    if (isOpen && showCloseButton && !isDialog) {
      const timer = setTimeout(() => {
        onClose();
      }, autoCloseTime);
      return () => clearTimeout(timer);
    }
  }, [isOpen, autoCloseTime, onClose, isDialog, showCloseButton]);

  if (!isOpen) return null;

  return ReactDOM.createPortal(
    <div className={`modal ${showCloseButton ? "notification" : "form"} ${isDialog ? "dialog" : ""}`}>
      {(isDialog || !showCloseButton) && <div className="overlay" onClick={onClose}></div>}
      <div className="content">
        {message && <p>{message}</p>}
        {children}
        {isDialog && (
          <div className="dialog-buttons">
            <button className="confirm-btn" onClick={onConfirm}>{confirmText}</button>            
            <button className="cancel-btn" onClick={onClose}>{cancelText}</button>
          </div>
        )}
        {showCloseButton && (
          <button className="close-btn" onClick={onClose}><IoCloseOutline className="icon" /></button>
        )}
      </div>
    </div>,
    document.body
  );
};

export default Modal;
