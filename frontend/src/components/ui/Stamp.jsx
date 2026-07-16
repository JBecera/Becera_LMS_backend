function Stamp({ value, label, tone = "" }) {
  const color = tone === "danger" ? "var(--burgundy)" : tone === "warn" ? "var(--amber)" : "var(--accent-dark)";
  return (
    <div className="due-stamp" style={{ color }}>
      <div>
        <span>{value}</span>
        <small>{label}</small>
      </div>
    </div>
  );
}

export default Stamp;
