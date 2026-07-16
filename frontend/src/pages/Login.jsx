import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { loginMember } from "../services/memberService";

function Login() {
  const navigate = useNavigate();

  const [member, setMember] = useState({ email: "", password: "" });
  const [message, setMessage] = useState("");

  const handleChange = (e) => {
    setMember({ ...member, [e.target.name]: e.target.value });
  };

  const handleLogin = async () => {
    setMessage("");
    try {
      const res = await loginMember(member);
      const userPayload = {
        id: res.data.id,
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        email: res.data.email,
        role: res.data.role,
      };
      localStorage.setItem("user", JSON.stringify(userPayload));
      localStorage.setItem("token", res.data.token || "");

      const role = userPayload.role?.toUpperCase();
      if (role === "ADMIN") navigate("/admin");
      else if (role === "LIBRARIAN") navigate("/librarian");
      else navigate("/dashboard");
    } catch (error) {
      console.error(error);
      setMessage(error.response?.data?.error || "Invalid credentials");
    }
  };

  return (
    <div className="auth-shell">
      <section className="auth-side">
        <span className="brass-mark">LRBMS · Est. Reading Room</span>
        <div>
          <h1>Every title, tracked to the day it's due.</h1>
          <p>Sign back in to manage reservations, checkouts, and your library record from one place.</p>
        </div>
        <div className="catalog-stat">
          <div>
            <strong>3</strong>
            <span>User roles</span>
          </div>
          <div>
            <strong>24/7</strong>
            <span>Catalog access</span>
          </div>
          <div>
            <strong>&lt;1s</strong>
            <span>Search response</span>
          </div>
        </div>
      </section>

      <div className="auth-page">
        <section className="auth-card">
          <div className="card-header">
            <p className="eyebrow">Sign in</p>
            <h2>Welcome back</h2>
            <p>Enter your account details to continue to the campus library system.</p>
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
              placeholder="Enter your password"
              value={member.password}
              onChange={handleChange}
            />
          </div>

          {message ? <p className="message-banner error">{message}</p> : null}

          <div className="form-footer">
            <Button
              text="Login"
              onClick={handleLogin}
              disabled={!member.email || !member.password}
              variant="primary"
            />
            <p>
              New to the library system? <Link to="/">Create an account</Link>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}

export default Login;
