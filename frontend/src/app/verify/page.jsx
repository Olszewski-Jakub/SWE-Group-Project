import VerifyClient from '../../features/auth/pages/VerifyClient';

export default function VerifyPage({ searchParams }) {
  const token = typeof searchParams?.token === 'string' ? searchParams.token : '';
  return <VerifyClient token={token} />;
}
