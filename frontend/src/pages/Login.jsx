import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Button from "../components/Button";
import { loginMember } from "../services/memberService";

function Login() {
  const navigate = useNavigate();

  const [member, setMember] = useState({
    email: "",
    password: "",
  });

  const handleChange = (e) => {
    setMember({
      ...member,
      [e.target.name]: e.target.value,
    });
  };

  const handleLogin = async () => {
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

      const role = userPayload.role?.toUpperCase();
      if (role === "ADMIN") {
        navigate("/admin");
      } else if (role === "LIBRARIAN") {
        navigate("/librarian");
      } else {
        navigate("/dashboard");
      }
    } catch (error) {
      console.error(error);
      alert(error.response?.data?.error || "Invalid credentials");
    }
  };

  return (
    <div className="auth-page">
      <section className="hero">
        <div>
          <p className="eyebrow">Library Resource Management</p>
          <h1>Login to your booking dashboard</h1>
          <p className="hero-copy">
            Sign in to manage reservations, monitor borrowed items, and stay on top of your library activity.
          </p>
        </div>
      </section>

      <section className="auth-card">
        <div className="card-header">
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
  );
}

export default Login;
