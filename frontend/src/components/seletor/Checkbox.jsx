import "./Checkbox.css";

function Checkbox({
  label,
  checked,
  onChange,
  disabled = false,
  name,
  value,
}) {
  return (
    <label className="checkbox-container">
      <input
        className="checkbox-input"
        type="checkbox"
        name={name}
        value={value}
        checked={checked}
        onChange={onChange}
        disabled={disabled}
      />

      <span className="checkbox-custom" aria-hidden="true">
        {checked && (
          <svg
            viewBox="0 0 16 16"
            fill="none"
            className="checkbox-check"
          >
            <path
              d="M3 8L6.5 11.5L13 4.5"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        )}
      </span>

      {label && (
        <span className="checkbox-label">{label}</span>
      )}
    </label>
  );
}

export default Checkbox;