import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import { getMember, updateMember } from "../services/memberService";

function Account() {
  const storedUser = JSON.parse(localStorage.getItem("user")) || {};
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    address: "",
    password: "",
  });
  const [studentId, setStudentId] = useState("");
  const [dateRegistered, setDateRegistered] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!storedUser.id) return;
    getMember(storedUser.id)
      .then((res) => {
        const account = res.data;
        setForm({
          firstName: account.firstName || "",
          lastName: account.lastName || "",
          email: account.email || "",
          phoneNumber: account.phoneNumber || "",
          address: account.address || "",
          password: "",
        });
        setStudentId(account.studentId || "");
        setDateRegistered(account.dateRegistered || "");
      })
      .catch(() => setMessage("Unable to load your account details."));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    try {
      const payload = { ...form };
      if (!payload.password) {
        delete payload.password;
      }
      const res = await updateMember(storedUser.id, payload);
      const updatedUser = {
        ...storedUser,
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        email: res.data.email,
      };
      localStorage.setItem("user", JSON.stringify(updatedUser));
      setForm((current) => ({ ...current, password: "" }));
      setMessage("Account updated successfully.");
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to update your account.");
    }
  };

  return (
    <AppLayout
      eyebrow="Account"
      title="My account"
      description="Update your contact details and password. Borrowing history and fines are read-only records."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {studentId ? (
          <p className="panel-sub">Student ID: <span className="mono">{studentId}</span>{dateRegistered ? ` · Registered ${dateRegistered}` : ""}</p>
        ) : null}
        <form onSubmit={handleSubmit} className="form-grid">
          <input className="form-input" name="firstName" placeholder="First name" value={form.firstName} onChange={handleChange} required />
          <input className="form-input" name="lastName" placeholder="Last name" value={form.lastName} onChange={handleChange} required />
          <input className="form-input" name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input className="form-input" name="phoneNumber" placeholder="Phone number" value={form.phoneNumber} onChange={handleChange} />
          <input className="form-input" name="address" placeholder="Address" value={form.address} onChange={handleChange} style={{ gridColumn: "1 / -1" }} />
          <input className="form-input" name="password" type="password" placeholder="New password (leave blank to keep current)" value={form.password} onChange={handleChange} />
          <button className="button primary auto" type="submit" style={{ gridColumn: "1 / -1", justifySelf: "start" }}>
            Save changes
          </button>
        </form>
      </section>
    </AppLayout>
  );
}

export default Account;
