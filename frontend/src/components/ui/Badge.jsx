function Badge({ status, children }) {
  const tone = (status || "").toString().toLowerCase();
  return <span className={`badge ${tone}`}>{children || status}</span>;
}

export default Badge;
