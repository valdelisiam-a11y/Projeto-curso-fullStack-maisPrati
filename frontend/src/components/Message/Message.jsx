import './Message.css'

function Message({ type = 'info', children, className = '', ...props }) {
  if (children == null || children === '' || children === false) return null

  const icon = type === 'success' ? '✓' : type === 'error' ? '!' : 'i'

  return (
    <div
      {...props}
      className={`df-message df-message--${type} ${className}`.trim()}
      role={type === 'error' ? 'alert' : 'status'}
      aria-live={type === 'error' ? 'assertive' : 'polite'}
      aria-atomic="true"
    >
      <span className="df-message__icon" aria-hidden="true">
        {icon}
      </span>
      <span className="df-message__content">{children}</span>
    </div>
  )
}

export default Message