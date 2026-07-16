function StatCard({ label, value, hint, tone = "" }) {
  return (
    <article className={`stat-card ${tone}`}>
      <p className="stat-label">{label}</p>
      <p className="metric">{value}</p>
      {hint ? <p className="hint">{hint}</p> : null}
    </article>
  );
}

export default StatCard;
