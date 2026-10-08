import { forwardRef } from 'react'
import './Button.css'

const Button = forwardRef(function Button(
  {
    children,
    type = 'button',
    disabled = false,
    variant = 'primary',
    className = '',
    ...props
  },
  ref,
) {
  return (
    <button
      {...props}
      ref={ref}
      type={type}
      disabled={disabled}
      className={`df-button df-button--${variant} ${className}`.trim()}
    >
      {children}
    </button>
  )
})

Button.displayName = 'DocFlowButton'

export default Button