import { forwardRef, useId, useState } from 'react'
import './Input.css'

const Input = forwardRef(function Input(
  {
    label,
    type = 'text',
    name,
    value,
    onChange,
    placeholder,
    id,
    error,
    hint,
    className = '',
    ...props
  },
  ref,
) {
  const generatedId = useId()
  const inputId = id ?? name ?? generatedId
  const errorId = `${inputId}-error`
  const hintId = `${inputId}-hint`
  const [passwordVisible, setPasswordVisible] = useState(false)
  const isPassword = type === 'password'
  const describedBy = [
    props['aria-describedby'],
    error ? errorId : hint ? hintId : undefined,
  ].filter(Boolean).join(' ') || undefined

  return (
    <div className="df-field">
      <label className="df-field__label" htmlFor={inputId}>
        {label}
      </label>
      <div className="df-field__control">
        <input
          {...props}
          ref={ref}
          id={inputId}
          type={isPassword && passwordVisible ? 'text' : type}
          name={name}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          aria-invalid={error ? true : props['aria-invalid']}
          aria-describedby={describedBy}
          className={`df-field__input${isPassword ? ' df-field__input--password' : ''}${error ? ' df-field__input--invalid' : ''} ${className}`.trim()}
        />
        {isPassword && (
          <button
            type="button"
            className="df-field__toggle"
            aria-label={passwordVisible ? 'Ocultar senha' : 'Exibir senha'}
            aria-pressed={passwordVisible}
            disabled={props.disabled}
            onClick={() => setPasswordVisible((visible) => !visible)}
          >
            {passwordVisible ? 'Ocultar' : 'Mostrar'}
          </button>
        )}
      </div>
      {error ? (
        <span className="df-field__error" id={errorId}>
          {error}
        </span>
      ) : hint ? (
        <span className="df-field__hint" id={hintId}>
          {hint}
        </span>
      ) : null}
    </div>
  )
})

Input.displayName = 'DocFlowInput'

export default Input