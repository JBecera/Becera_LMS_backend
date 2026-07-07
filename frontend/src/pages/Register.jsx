import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { addMember } from "../services/memberService";

function Register() {
  const navigate = useNavigate();

  const [member, setMember] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });

  const handleChange = (e) => {
    setMember({
      ...member,
      [e.target.name]: e.target.value,
    });
  };

  const handleRegister = async () => {
    try {
      await addMember({ ...member, role: "MEMBER" });
      alert("Registration successful!");
      navigate("/login");
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.error || "Registration failed.");
    }
  };

  return (
    <div className="auth-page">
      <section className="hero">
        <div>
          <p className="eyebrow">Library Resource Booking</p>
          <h1>Register for your library management account</h1>
          <p className="hero-copy">
            Access study room reservations, manage borrowings, and keep your library bookings organized in one smart dashboard.
          </p>
        </div>
      </section>

      <section className="auth-card">
        <div className="card-header">
          <h2>Create a secure account</h2>
          <p>Start managing your reservations, resources, and library access with confidence.</p>
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="firstName">First name</label>
          <input
            id="firstName"
            className="form-input"
            type="text"
            name="firstName"
            placeholder="Jane"
            value={member.firstName}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="lastName">Last name</label>
          <input
            id="lastName"
            className="form-input"
            type="text"
            name="lastName"
            placeholder="Doe"
            value={member.lastName}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="email">Email address</label>
          <input
            id="email"
            className="form-input"
            type="email"
            name="email"
            placeholder="jane.doe@library.edu"
            value={member.email}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="password">Password</label>
          <input
            id="password"
            className="form-input"
            type="password"
            name="password"
            placeholder="Create a strong password"
            value={member.password}
            onChange={handleChange}
          />
        </div>

        <div className="form-footer">
          <Button
            text="Create account"
            onClick={handleRegister}
            disabled={
              !member.firstName ||
              !member.lastName ||
              !member.email ||
              !member.password
            }
            variant="primary"
          />
          <p>
            Already have an account? <Link to="/login">Login now</Link>
          </p>
        </div>
      </section>
    </div>
  );
}

export default Register;
