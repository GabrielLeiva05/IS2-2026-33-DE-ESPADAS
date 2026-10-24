import { useAuth0 } from "@auth0/auth0-react";

const CallbackPage = () => {
  const { error } = useAuth0();

  if (error) {
    return (
      <div className="text-center font-semibold text-red-500">
        Error de Auth0: {error.message}
      </div>
    );
  }

  return <div></div>;
};

export default CallbackPage;