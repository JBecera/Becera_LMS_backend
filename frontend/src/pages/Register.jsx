import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { addMember } from "../services/memberService";
import { PASSWORD_HINT, passwordStrengthError } from "../utils/password";

function Register() {
  const navigate = useNavigate();

  const [member, setMember] = useState({ firstName: "", lastName: "", email: "", password: "" });
  const [message, setMessage] = useState("");

  const handleChange = (e) => {
    setMember({ ...member, [e.target.name]: e.target.value });
  };

  const handleRegister = async () => {
    setMessage("");
    const passwordError = passwordStrengthError(member.password);
    if (passwordError) {
      setMessage(passwordError);
      return;
    }
    try {
      await addMember({ ...member, role: "MEMBER" });
      setMessage("Registration successful. You can sign in now.");
      setTimeout(() => navigate("/login"), 800);
    } catch (error) {
      console.error(error);
      setMessage(error.response?.data?.error || "Registration failed.");
    }
  };

  return (
    <div className="auth-shell">
      <section className="auth-side">
        <span className="brass-mark">LRBMS · Est. Reading Room</span>
        <div>
          <h1>Your library card, reimagined as a dashboard.</h1>
          <p>Register once to search the catalog, reserve resources, and follow every due date in real time.</p>
        </div>
        <div className="catalog-stat">
          <div>
            <strong>3</strong>
            <span>Items you can borrow</span>
          </div>
          <div>
            <strong>0₱</strong>
            <span>To register</span>
          </div>
          <div>
            <strong>24/7</strong>
            <span>Catalog access</span>
          </div>
        </div>
      </section>

      <div className="auth-page">
        <section className="auth-card">
          <div className="card-header">
            <p className="eyebrow">Create account</p>
            <h2>Join the library</h2>
            <p>Start managing your reservations, resources, and borrowing history with confidence.</p>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="firstName">First name</label>
            <input id="firstName" className="form-input" type="text" name="firstName" placeholder="Jane" value={member.firstName} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="lastName">Last name</label>
            <input id="lastName" className="form-input" type="text" name="lastName" placeholder="Doe" value={member.lastName} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email address</label>
            <input id="email" className="form-input" type="email" name="email" placeholder="jane.doe@library.edu" value={member.email} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input id="password" className="form-input" type="password" name="password" placeholder="Create a strong password" value={member.password} onChange={handleChange} />
            <p className="field-hint">{PASSWORD_HINT}</p>
          </div>

          {message ? <p className={`message-banner ${message.includes("successful") ? "" : "error"}`}>{message}</p> : null}

          <div className="form-footer">
            <Button
              text="Create account"
              onClick={handleRegister}
              disabled={!member.firstName || !member.lastName || !member.email || !member.password}
              variant="primary"
            />
            <p>
              Already have an account? <Link to="/login">Login now</Link>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}

export default Register;
