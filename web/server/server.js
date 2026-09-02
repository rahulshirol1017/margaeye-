require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { OAuth2Client } = require('google-auth-library');
const jwt = require('jsonwebtoken');

const app = express();
const PORT = process.env.PORT || 5000;
const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID;
const JWT_SECRET = process.env.JWT_SECRET || 'antigravity_secret_key';

const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

// Middleware
app.use(cors());
app.use(express.json());

// In-Memory Database (Replace with PostgreSQL, MongoDB, or MySQL as needed)
const usersDatabase = new Map();

/**
 * 1. GOOGLE OAUTH VERIFICATION ENDPOINT
 * Receives 'idToken' from Android App Credential Manager
 */
app.post('/api/auth/google', async (req, res) => {
  try {
    const { idToken } = req.body;

    if (!idToken) {
      return res.status(400).json({ success: false, message: 'Google ID token is required' });
    }

    // Verify token with Google's servers
    const ticket = await googleClient.verifyIdToken({
      idToken: idToken,
      audience: GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const { sub: googleId, email, name, picture } = payload;

    // Check if user exists in database, or create a new user record
    let user = usersDatabase.get(email);
    if (!user) {
      user = {
        id: googleId,
        email: email,
        name: name || 'Google User',
        picture: picture || '',
        provider: 'google',
        createdAt: new Date().toISOString()
      };
      usersDatabase.set(email, user);
      console.log(`[DB] Created new user via Google OAuth: ${email}`);
    } else {
      console.log(`[DB] Logged in existing Google user: ${email}`);
    }

    // Issue JWT Session Token
    const sessionToken = jwt.sign(
      { userId: user.id, email: user.email, name: user.name },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    return res.status(200).json({
      success: true,
      message: 'Google authentication successful',
      token: sessionToken,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        picture: user.picture
      }
    });

  } catch (error) {
    console.error('[OAuth Error] Failed to verify Google Token:', error.message);
    return res.status(401).json({
      success: false,
      message: 'Invalid Google ID token',
      error: error.message
    });
  }
});

/**
 * 2. EMAIL SIGN IN ENDPOINT
 */
app.post('/api/auth/signin', (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ success: false, message: 'Email and password are required' });
  }

  const user = usersDatabase.get(email);
  if (!user) {
    return res.status(401).json({ success: false, message: 'Invalid email or password' });
  }

  const token = jwt.sign(
    { userId: user.id, email: user.email, name: user.name },
    JWT_SECRET,
    { expiresIn: '7d' }
  );

  return res.status(200).json({
    success: true,
    message: 'Signed in successfully',
    token,
    user
  });
});

/**
 * 3. EMAIL SIGN UP ENDPOINT
 */
app.post('/api/auth/signup', (req, res) => {
  const { name, email, password } = req.body;

  if (!name || !email || !password) {
    return res.status(400).json({ success: false, message: 'Name, email, and password are required' });
  }

  if (usersDatabase.has(email)) {
    return res.status(400).json({ success: false, message: 'An account with this email already exists' });
  }

  const newUser = {
    id: 'user_' + Date.now(),
    name,
    email,
    provider: 'email',
    createdAt: new Date().toISOString()
  };

  usersDatabase.set(email, newUser);

  const token = jwt.sign(
    { userId: newUser.id, email: newUser.email, name: newUser.name },
    JWT_SECRET,
    { expiresIn: '7d' }
  );

  return res.status(201).json({
    success: true,
    message: 'Account created successfully',
    token,
    user: newUser
  });
});

/**
 * 4. GET CURRENT USER PROFILE
 */
app.get('/api/auth/me', (req, res) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: 'Unauthorized' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const user = usersDatabase.get(decoded.email);
    return res.status(200).json({ success: true, user });
  } catch (err) {
    return res.status(401).json({ success: false, message: 'Invalid session token' });
  }
});

// Start Server
app.listen(PORT, () => {
  console.log(`=================================================`);
  console.log(`🚀 Antigravity Auth Node.js Server Running`);
  console.log(`🌐 Server URL: http://localhost:${PORT}`);
  console.log(`🔑 Configured Google Client ID: ${GOOGLE_CLIENT_ID}`);
  console.log(`=================================================`);
});
