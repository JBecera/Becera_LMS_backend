import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";

import Dashboard from "./pages/Dashboard";
import Catalog from "./pages/Catalog";
import BorrowConfirmation from "./pages/BorrowConfirmation";
import ReservationConfirmation from "./pages/ReservationConfirmation";
import MyBorrowing from "./pages/MyBorrowing";
import Reservations from "./pages/Reservations";
import BorrowingHistory from "./pages/BorrowingHistory";
import Fines from "./pages/Fines";
import Account from "./pages/Account";

import LibrarianDashboard from "./pages/LibrarianDashboard";
import ManageCatalog from "./pages/ManageCatalog";
import Transactions from "./pages/Transactions";
import Members from "./pages/Members";

import AdminDashboard from "./pages/AdminDashboard";
import ManageAccounts from "./pages/ManageAccounts";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Register />} />
        <Route path="/login" element={<Login />} />

        {/* Member */}
        <Route path="/dashboard" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Dashboard /></ProtectedRoute>} />
        <Route path="/catalog" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Catalog /></ProtectedRoute>} />
        <Route path="/catalog/:bookId/borrow" element={<ProtectedRoute allowedRoles={["MEMBER"]}><BorrowConfirmation /></ProtectedRoute>} />
        <Route path="/catalog/:bookId/reserve" element={<ProtectedRoute allowedRoles={["MEMBER"]}><ReservationConfirmation /></ProtectedRoute>} />
        <Route path="/my-borrowing" element={<ProtectedRoute allowedRoles={["MEMBER"]}><MyBorrowing /></ProtectedRoute>} />
        <Route path="/reservations" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Reservations /></ProtectedRoute>} />
        <Route path="/history" element={<ProtectedRoute allowedRoles={["MEMBER"]}><BorrowingHistory /></ProtectedRoute>} />
        <Route path="/fines" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Fines /></ProtectedRoute>} />

        {/* Librarian */}
        <Route path="/librarian" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><LibrarianDashboard /></ProtectedRoute>} />
        <Route path="/librarian/catalog" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><ManageCatalog /></ProtectedRoute>} />
        <Route path="/librarian/transactions" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><Transactions /></ProtectedRoute>} />
        <Route path="/librarian/reservations" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><Reservations /></ProtectedRoute>} />
        <Route path="/librarian/members" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><Members /></ProtectedRoute>} />
        <Route path="/librarian/fines" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><Fines /></ProtectedRoute>} />

        {/* Admin */}
        <Route path="/admin" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminDashboard /></ProtectedRoute>} />
        <Route path="/admin/accounts" element={<ProtectedRoute allowedRoles={["ADMIN"]}><ManageAccounts /></ProtectedRoute>} />

        {/* Shared */}
        <Route path="/account" element={<ProtectedRoute allowedRoles={["MEMBER", "LIBRARIAN", "ADMIN"]}><Account /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
