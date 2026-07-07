import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function LibrarianDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");

  const [resources, setResources] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [resourceForm, setResourceForm] = useState({ id: null, name: "", type: "Room" });
  const [message, setMessage] = useState("");

  const loadData = async () => {
    try {
      const [resourcesRes, bookingsRes] = await Promise.all([
        api.get("/resources"),
        api.get("/bookings"),
      ]);
      setResources(resourcesRes.data);
      setBookings(bookingsRes.data);
    } catch (error) {
      console.error(error);
      setMessage("Unable to load librarian resources and bookings.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const handleResourceChange = (event) => {
    const { name, value } = event.target;
    setResourceForm((current) => ({ ...current, [name]: value }));
  };

  const handleResourceSubmit = async (event) => {
    event.preventDefault();
    try {
      if (resourceForm.id) {
        await api.put(`/resources/${resourceForm.id}`, resourceForm);
        setMessage("Resource updated successfully.");
      } else {
        await api.post("/resources", resourceForm);
        setMessage("Resource created successfully.");
      }
      setResourceForm({ id: null, name: "", type: "Room" });
      loadData();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to save resource.");
    }
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Librarian Workspace</p>
          <h1>Hello, {user?.firstName || "Librarian"}.</h1>
          <p>Manage library resources, review booking requests, and keep daily operations on track.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      {message ? <p className="message-banner">{message}</p> : null}

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Resources</h2>
          <p className="metric">{resources.length}</p>
          <p className="card-copy">Available study rooms, equipment, and library materials.</p>
        </article>
        <article className="dashboard-card">
          <h2>Bookings</h2>
          <p className="metric">{bookings.length}</p>
          <p className="card-copy">Member booking requests currently managed by you.</p>
        </article>
      </section>

      <section className="dashboard-card" style={{ marginTop: "1.5rem" }}>
        <h2>Add or update a resource</h2>
        <form onSubmit={handleResourceSubmit} className="form-grid">
          <input className="form-input" name="name" placeholder="Resource name" value={resourceForm.name} onChange={handleResourceChange} required />
          <input className="form-input" name="type" placeholder="Resource type" value={resourceForm.type} onChange={handleResourceChange} required />
          <button className="button primary" type="submit">{resourceForm.id ? "Save resource" : "Create resource"}</button>
        </form>
      </section>

      <section className="dashboard-card" style={{ marginTop: "1.5rem" }}>
        <h2>Booking queue</h2>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Resource</th>
                <th>Booked by</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((booking) => (
                <tr key={booking.id}>
                  <td>{booking.resourceName || booking.resource?.name || "Resource"}</td>
                  <td>{booking.memberName || booking.member?.firstName || "Member"}</td>
                  <td>{booking.status || "PENDING"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default LibrarianDashboard;
