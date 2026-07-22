import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Icon from "../components/ui/Icon";
import { getMember, updateMember, changePassword } from "../services/memberService";
import { PASSWORD_HINT, passwordStrengthError } from "../utils/password";

const initialProfile = { firstName: "", lastName: "", email: "", phoneNumber: "", address: "" };
const initialPasswordForm = { currentPassword: "", newPassword: "", confirmPassword: "" };

function Account() {
  const storedUser = JSON.parse(localStorage.getItem("user")) || {};
  const [activeTab, setActiveTab] = useState("profile");

  const [profile, setProfile] = useState(initialProfile);
  const [memberId, setMemberId] = useState("");
  const [dateRegistered, setDateRegistered] = useState("");
  const [profileMessage, setProfileMessage] = useState(null);
  const [profileSaving, setProfileSaving] = useState(false);

  const [passwordForm, setPasswordForm] = useState(initialPasswordForm);
  const [passwordErrors, setPasswordErrors] = useState({});
  const [passwordMessage, setPasswordMessage] = useState(null);
  const [passwordSaving, setPasswordSaving] = useState(false);

  useEffect(() => {
    if (!storedUser.id) return;
    getMember(storedUser.id)
      .then((res) => {
        const account = res.data;
        setProfile({
          firstName: account.firstName || "",
          lastName: account.lastName || "",
          email: account.email || "",
          phoneNumber: account.phoneNumber || "",
          address: account.address || "",
        });
        setMemberId(account.memberId || "");
        setDateRegistered(account.dateRegistered || "");
      })
      .catch(() => setProfileMessage({ type: "error", text: "Unable to load your account details." }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleProfileChange = (event) => {
    const { name, value } = event.target;
    setProfile((current) => ({ ...current, [name]: value }));
  };

  const handleProfileSubmit = async (event) => {
    event.preventDefault();
    setProfileMessage(null);
    setProfileSaving(true);
    try {
      const res = await updateMember(storedUser.id, profile);
      const updatedUser = {
        ...storedUser,
        firstName: res.data.firstName,
        lastName: res.data.lastName,
        email: res.data.email,
      };
      localStorage.setItem("user", JSON.stringify(updatedUser));
      setProfileMessage({ type: "success", text: "Profile updated successfully." });
    } catch (error) {
      setProfileMessage({ type: "error", text: error.response?.data?.error || "Unable to update your profile." });
    } finally {
      setProfileSaving(false);
    }
  };

  const handlePasswordFieldChange = (event) => {
    const { name, value } = event.target;
    setPasswordForm((current) => ({ ...current, [name]: value }));
    setPasswordErrors((current) => ({ ...current, [name]: undefined }));
  };

  const validatePasswordForm = () => {
    const errors = {};
    if (!passwordForm.currentPassword) {
      errors.currentPassword = "Enter your current password.";
    }
    if (!passwordForm.newPassword) {
      errors.newPassword = "Enter a new password.";
    } else if (passwordStrengthError(passwordForm.newPassword)) {
      errors.newPassword = passwordStrengthError(passwordForm.newPassword);
    } else if (passwordForm.currentPassword && passwordForm.newPassword === passwordForm.currentPassword) {
      errors.newPassword = "New password must be different from your current password.";
    }
    if (!passwordForm.confirmPassword) {
      errors.confirmPassword = "Confirm your new password.";
    } else if (passwordForm.newPassword && passwordForm.newPassword !== passwordForm.confirmPassword) {
      errors.confirmPassword = "Passwords do not match.";
    }
    return errors;
  };

  const handlePasswordSubmit = async (event) => {
    event.preventDefault();
    setPasswordMessage(null);
    const errors = validatePasswordForm();
    setPasswordErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setPasswordSaving(true);
    try {
      await changePassword(storedUser.id, passwordForm);
      setPasswordForm(initialPasswordForm);
      setPasswordMessage({ type: "success", text: "Password changed successfully." });
    } catch (error) {
      setPasswordMessage({ type: "error", text: error.response?.data?.error || "Unable to change your password." });
    } finally {
      setPasswordSaving(false);
    }
  };

  return (
    <AppLayout
      eyebrow="Account"
      title="My account"
      description="Manage your contact details and account security. Borrowing history and fines are read-only records."
    >
      <section className="panel-card" style={{ marginTop: 0 }}>
        {memberId ? (
          <p className="panel-sub">
            Member ID: <span className="mono">{memberId}</span>
            {dateRegistered ? ` · Registered ${dateRegistered}` : ""}
          </p>
        ) : null}

        <div className="section-tabs">
          <button
            type="button"
            className={`section-tab${activeTab === "profile" ? " active" : ""}`}
            onClick={() => setActiveTab("profile")}
          >
            Profile
          </button>
          <button
            type="button"
            className={`section-tab${activeTab === "security" ? " active" : ""}`}
            onClick={() => setActiveTab("security")}
          >
            Security
          </button>
        </div>

        {activeTab === "profile" ? (
          <>
            {profileMessage ? (
              <p className={`message-banner${profileMessage.type === "error" ? " error" : ""}`}>{profileMessage.text}</p>
            ) : null}
            <form onSubmit={handleProfileSubmit} className="form-grid">
              <div className="form-group">
                <label className="form-label" htmlFor="firstName">First name</label>
                <input id="firstName" className="form-input" name="firstName" value={profile.firstName} onChange={handleProfileChange} required />
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="lastName">Last name</label>
                <input id="lastName" className="form-input" name="lastName" value={profile.lastName} onChange={handleProfileChange} required />
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="email">Email address</label>
                <input id="email" className="form-input" name="email" type="email" value={profile.email} onChange={handleProfileChange} required />
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="phoneNumber">Phone number</label>
                <input id="phoneNumber" className="form-input" name="phoneNumber" value={profile.phoneNumber} onChange={handleProfileChange} />
              </div>
              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label className="form-label" htmlFor="address">Address</label>
                <input id="address" className="form-input" name="address" value={profile.address} onChange={handleProfileChange} />
              </div>
              <div style={{ gridColumn: "1 / -1" }}>
                <button className="button primary auto" type="submit" disabled={profileSaving}>
                  {profileSaving ? "Saving…" : "Save changes"}
                </button>
              </div>
            </form>
          </>
        ) : (
          <>
            <p className="security-note">
              <Icon name="lock" size={15} />
              Changing your password requires your current password, so no one can hijack your account from a shared or unlocked device.
            </p>
            {passwordMessage ? (
              <p className={`message-banner${passwordMessage.type === "error" ? " error" : ""}`}>{passwordMessage.text}</p>
            ) : null}
            <form onSubmit={handlePasswordSubmit} className="form-grid" noValidate>
              <div className="form-group" style={{ gridColumn: "1 / -1" }}>
                <label className="form-label" htmlFor="currentPassword">Current password</label>
                <input
                  id="currentPassword"
                  className="form-input"
                  name="currentPassword"
                  type="password"
                  autoComplete="current-password"
                  value={passwordForm.currentPassword}
                  onChange={handlePasswordFieldChange}
                />
                {passwordErrors.currentPassword ? <p className="field-error">{passwordErrors.currentPassword}</p> : null}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="newPassword">New password</label>
                <input
                  id="newPassword"
                  className="form-input"
                  name="newPassword"
                  type="password"
                  autoComplete="new-password"
                  value={passwordForm.newPassword}
                  onChange={handlePasswordFieldChange}
                />
                {passwordErrors.newPassword ? (
                  <p className="field-error">{passwordErrors.newPassword}</p>
                ) : (
                  <p className="field-hint">{PASSWORD_HINT}</p>
                )}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="confirmPassword">Confirm new password</label>
                <input
                  id="confirmPassword"
                  className="form-input"
                  name="confirmPassword"
                  type="password"
                  autoComplete="new-password"
                  value={passwordForm.confirmPassword}
                  onChange={handlePasswordFieldChange}
                />
                {passwordErrors.confirmPassword ? <p className="field-error">{passwordErrors.confirmPassword}</p> : null}
              </div>
              <div style={{ gridColumn: "1 / -1" }}>
                <button className="button primary auto" type="submit" disabled={passwordSaving}>
                  {passwordSaving ? "Updating…" : "Update password"}
                </button>
              </div>
            </form>
          </>
        )}
      </section>
    </AppLayout>
  );
}

export default Account;
