import React from 'react';
import './Message.css';

const Message = ({ text, type = 'success' }) => {
  if (!text) return null;

  return (
    <div className={`docflow-message docflow-message-${type}`}>
      {text}
    </div>
  );
};

export default Message;