import { forwardRef, useId } from 'react'
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
  const describedBy = [
    props['aria-describedby'],
    error ? errorId : hint ? hintId : undefined,
  ].filter(Boolean).join(' ') || undefined

  return (
    <div className="df-field">
      <label className="df-field__label" htmlFor={inputId}>
        {label}
      </label>
      <input
        {...props}
        ref={ref}
        id={inputId}
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        aria-invalid={error ? true : props['aria-invalid']}
        aria-describedby={describedBy}
        className={`df-field__input${error ? ' df-field__input--invalid' : ''} ${className}`.trim()}
      />
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