import React from 'react';
import './Button.css';

const Button = ({ children, type = 'button', disabled = false, onClick, ...props }) => {
return (
<button
className="docflow-btn"
type={type}
disabled={disabled}
onClick={onClick}
{...props}
>
{children}

);
};

export default Button;