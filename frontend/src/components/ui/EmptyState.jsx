import Icon from "./Icon";

function EmptyState({ icon = "catalog", title, description }) {
  return (
    <div className="empty-state">
      <div className="plate">
        <Icon name={icon} size={20} />
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
    </div>
  );
}

export default EmptyState;
