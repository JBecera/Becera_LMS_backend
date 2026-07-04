function Button({ text, onClick, disabled, variant = "primary" }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`button ${variant}`}
    >
      {text}
    </button>
  );
}

export default Button;
