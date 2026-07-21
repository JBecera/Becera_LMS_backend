export const navByRole = {
  MEMBER: [
    { to: "/dashboard", label: "Dashboard", icon: "dashboard" },
    { to: "/catalog", label: "Catalog", icon: "catalog" },
    { to: "/my-borrowing", label: "My Borrowing", icon: "borrow" },
    { to: "/reservations", label: "Reservations", icon: "reserve" },
    { to: "/history", label: "Borrowing History", icon: "history" },
    { to: "/fines", label: "Fines", icon: "fines" },
    { to: "/account", label: "My Account", icon: "accounts" },
  ],
  LIBRARIAN: [
    { to: "/librarian", label: "Dashboard", icon: "dashboard" },
    { to: "/librarian/catalog", label: "Catalog", icon: "catalog" },
    { to: "/librarian/transactions", label: "Transactions", icon: "transactions" },
    { to: "/librarian/reservations", label: "Reservations", icon: "reserve" },
    { to: "/librarian/members", label: "Members", icon: "students" },
    { to: "/librarian/fines", label: "Fines", icon: "fines" },
    { to: "/account", label: "My Account", icon: "accounts" },
  ],
  ADMIN: [
    { to: "/admin", label: "Dashboard", icon: "dashboard" },
    { to: "/admin/accounts", label: "Accounts", icon: "accounts" },
    { to: "/account", label: "My Account", icon: "accounts" },
  ],
};

export const roleTag = {
  MEMBER: "Member",
  LIBRARIAN: "Librarian",
  ADMIN: "Administrator",
};
