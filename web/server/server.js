require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { OAuth2Client } = require('google-auth-library');
const jwt = require('jsonwebtoken');

const fs = require('fs');
const path = require('path');
const mongoose = require('mongoose');
const multer = require('multer');

const app = express();
const PORT = process.env.PORT || 5001;
const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID;
const JWT_SECRET = process.env.JWT_SECRET || 'antigravity_secret_key';
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/mplads_db';

const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

// Ensure uploads folder exists
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
}

// Multer Storage Configuration
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadsDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, 'geo-' + uniqueSuffix + ext);
  }
});
const upload = multer({ storage });

// Connect to MongoDB
let isMongoConnected = false;
mongoose.connect(MONGODB_URI)
  .then(() => {
    isMongoConnected = true;
    console.log(`[MongoDB] Connected successfully to: ${MONGODB_URI}`);
  })
  .catch((err) => {
    console.warn(`[MongoDB Warning] Could not connect to MongoDB: ${err.message}. (Server will remain operational)`);
  });

// GeoPhoto Mongoose Schema
const geoPhotoSchema = new mongoose.Schema({
  photoId: { type: String, required: true, unique: true },
  imageUrl: { type: String, required: true },
  rawImageUrl: { type: String },
  watermarkedImageUrl: { type: String },
  hasVisibleOverlay: { type: Boolean, default: true },
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
  accuracy: { type: Number, default: 0 },
  altitude: { type: Number, default: 0 },
  placeName: { type: String },
  workerName: { type: String },
  workId: { type: String },
  description: { type: String },
  dateFormatted: { type: String },
  timeFormatted: { type: String },
  capturedAt: { type: Date, default: Date.now }
}, { timestamps: true });

// Bind explicitly to 'geotagged_photos' collection
const GeoPhotoModel = mongoose.model('GeoPhoto', geoPhotoSchema, 'geotagged_photos');


// Middleware
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(uploadsDir));

// In-Memory Database (Fallback)
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

/**
 * 5. UPLOAD GEO-TAGGED PHOTO & METADATA TO MONGODB
 * Endpoint: POST /api/photos/upload
 * Expects multipart/form-data with 'photo' file and JSON string metadata or form fields
 */
app.post('/api/photos/upload', upload.single('photo'), async (req, res) => {
  try {
    const {
      photoId,
      latitude,
      longitude,
      accuracy,
      altitude,
      placeName,
      workerName,
      workId,
      description,
      dateFormatted,
      timeFormatted,
      capturedAt
    } = req.body;

    const file = req.file;
    if (!file && !req.body.imageUrl) {
      return res.status(400).json({ success: false, message: 'Photo image file is required' });
    }

    const host = req.get('host') || `localhost:${PORT}`;
    const protocol = req.protocol || 'http';
    const relativeUrl = file ? `/uploads/${file.filename}` : req.body.imageUrl;
    const fullImageUrl = `${protocol}://${host}${relativeUrl}`;

    const newPhotoData = {
      photoId: photoId || 'photo_' + Date.now(),
      imageUrl: fullImageUrl,
      rawImageUrl: fullImageUrl,
      hasVisibleOverlay: req.body.hasVisibleOverlay === 'true' || req.body.hasVisibleOverlay === true,
      latitude: parseFloat(latitude) || 0.0,
      longitude: parseFloat(longitude) || 0.0,
      accuracy: parseFloat(accuracy) || 0.0,
      altitude: altitude ? parseFloat(altitude) : 0.0,
      placeName: placeName || '',
      workerName: workerName || '',
      workId: workId || '',
      description: description || '',
      dateFormatted: dateFormatted || new Date().toLocaleDateString(),
      timeFormatted: timeFormatted || new Date().toLocaleTimeString(),
      capturedAt: capturedAt ? new Date(capturedAt) : new Date()
    };

    if (isMongoConnected) {
      const savedPhoto = await GeoPhotoModel.findOneAndUpdate(
        { photoId: newPhotoData.photoId },
        newPhotoData,
        { upsert: true, new: true }
      );
      console.log(`[MongoDB] Saved GeoPhoto record: ${savedPhoto.photoId} (${savedPhoto.workerName || 'No Worker'})`);
      return res.status(201).json({
        success: true,
        message: 'Photo and metadata saved to MongoDB successfully',
        photo: savedPhoto
      });
    } else {
      console.warn(`[Storage Warning] MongoDB not connected. Photo saved to server disk at ${relativeUrl}`);
      return res.status(201).json({
        success: true,
        message: 'Photo file saved to server disk (MongoDB offline)',
        photo: newPhotoData
      });
    }
  } catch (error) {
    console.error('[Upload Error] Failed to save photo:', error.message);
    return res.status(500).json({
      success: false,
      message: 'Failed to save photo to MongoDB',
      error: error.message
    });
  }
});

/**
 * 6. GET ALL GEO-TAGGED PHOTOS FROM MONGODB
 * Endpoint: GET /api/photos
 */
app.get('/api/photos', async (req, res) => {
  try {
    if (!isMongoConnected) {
      return res.status(503).json({ success: false, message: 'MongoDB service unavailable' });
    }
    const photos = await GeoPhotoModel.find().sort({ capturedAt: -1 });
    return res.status(200).json({
      success: true,
      count: photos.length,
      photos
    });
  } catch (error) {
    console.error('[Fetch Error] Failed to retrieve photos:', error.message);
    return res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * 7. DELETE PHOTO FROM MONGODB & DISK
 * Endpoint: DELETE /api/photos/:photoId
 */
app.delete('/api/photos/:photoId', async (req, res) => {
  try {
    const { photoId } = req.params;
    if (!isMongoConnected) {
      return res.status(503).json({ success: false, message: 'MongoDB service unavailable' });
    }
    const photo = await GeoPhotoModel.findOneAndDelete({ photoId });
    if (!photo) {
      return res.status(404).json({ success: false, message: 'Photo not found' });
    }
    return res.status(200).json({ success: true, message: 'Photo deleted successfully' });
  } catch (error) {
    return res.status(500).json({ success: false, error: error.message });
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
