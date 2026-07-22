import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ToastProvider } from "./components/ui/ToastProvider";
import Register from "./pages/Register";
import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";

import Dashboard from "./pages/Dashboard";
import Catalog from "./pages/Catalog";
import BookingConfirmation from "./pages/BookingConfirmation";
import MyBookings from "./pages/MyBookings";
import Fines from "./pages/Fines";
import Account from "./pages/Account";

import LibrarianDashboard from "./pages/LibrarianDashboard";
import ManageCatalog from "./pages/ManageCatalog";
import ManageBookings from "./pages/ManageBookings";
import Members from "./pages/Members";

import AdminDashboard from "./pages/AdminDashboard";
import ManageAccounts from "./pages/ManageAccounts";

function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Routes>
        <Route path="/" element={<Register />} />
        <Route path="/login" element={<Login />} />

        {/* Member */}
        <Route path="/dashboard" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Dashboard /></ProtectedRoute>} />
        <Route path="/catalog" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Catalog /></ProtectedRoute>} />
        <Route path="/catalog/:bookId/reserve" element={<ProtectedRoute allowedRoles={["MEMBER"]}><BookingConfirmation /></ProtectedRoute>} />
        <Route path="/bookings" element={<ProtectedRoute allowedRoles={["MEMBER"]}><MyBookings /></ProtectedRoute>} />
        <Route path="/fines" element={<ProtectedRoute allowedRoles={["MEMBER"]}><Fines /></ProtectedRoute>} />

        {/* Librarian */}
        <Route path="/librarian" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><LibrarianDashboard /></ProtectedRoute>} />
        <Route path="/librarian/catalog" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><ManageCatalog /></ProtectedRoute>} />
        <Route path="/librarian/bookings" element={<ProtectedRoute allowedRoles={["LIBRARIAN"]}><ManageBookings /></ProtectedRoute>} />
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
    </ToastProvider>
  );
}

export default App;
