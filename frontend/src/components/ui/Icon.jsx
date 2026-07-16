const paths = {
  dashboard: "M4 4h7v7H4V4zm9 0h7v4h-7V4zm0 7h7v9h-7v-9zM4 14h7v6H4v-6z",
  catalog: "M5 4.5C5 3.7 5.7 3 6.5 3H18a1 1 0 0 1 1 1v15.5a1 1 0 0 1-1 1H6.5A1.5 1.5 0 0 1 5 19V4.5zM7 3v17M9 7h7M9 10.5h7",
  borrow: "M4 12a8 8 0 1 1 3 6.2M4 12v5h5",
  reserve: "M6 3.5A1.5 1.5 0 0 1 7.5 2h9A1.5 1.5 0 0 1 18 3.5V21l-6-3.6L6 21V3.5z",
  history: "M12 7v5l3.2 2M20 12a8 8 0 1 1-2.6-5.9M20 4v4h-4",
  fines: "M6 3h9l3 3v15H6V3zM15 3v3h3M9 12h6M9 15.5h6M9 8.5h3",
  transactions: "M7 7h13l-3-3M17 17H4l3 3M17 7v10",
  students: "M4 19c0-2.8 2.7-5 6-5s6 2.2 6 5M10 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM17.5 13.2c2.1.5 3.5 2 3.5 3.8",
  accounts: "M4 20V6.5C4 5.1 5.1 4 6.5 4H15l5 5v11H4zM15 4v5h5M8 12h8M8 15.5h8",
  logout: "M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3M15 8l4 4-4 4M19 12H9",
};

function Icon({ name, size = 18, ...rest }) {
  const d = paths[name];
  if (!d) return null;
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      {...rest}
    >
      <path d={d} />
    </svg>
  );
}

export default Icon;
