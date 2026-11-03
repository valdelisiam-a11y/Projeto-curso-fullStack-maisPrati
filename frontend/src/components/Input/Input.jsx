import React from 'react';
import './Input.css';

const Input = ({ label, type = 'text', name, value, onChange, placeholder, ...props }) => {
  return (
    <div className="docflow-input-container">
      {label && (
        <label htmlFor={name} className="docflow-label">
          {label}
        </label>
      )}
      <input
        id={name}
        className="docflow-input"
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        {...props}
      />
    </div>
  );
};

export default Input;