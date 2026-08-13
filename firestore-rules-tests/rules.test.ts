/**
 * Testy regul bezpieczenstwa Firestore dla Slownik Jezyka Trudnego.
 *
 * Wymagania:
 *   - Firebase Emulator Suite (firebase emulators:start)
 *   - npm install w tym katalogu
 *   - npm test
 */

import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from '@firebase/rules-unit-testing';
import { readFileSync } from 'fs';
import { afterAll, afterEach, beforeAll, describe, it } from 'vitest';
import { doc, getDoc, setDoc, deleteDoc } from 'firebase/firestore';

// UID-y uzywane w testach
const OWNER_UID = 'user-owner-123';
const OTHER_UID = 'user-other-456';

/** Poprawny dokument uzytkownika (wszystkie dozwolone pola). */
const VALID_USER_DOC = {
  progressMap: {
    word1: {
      wordId: 'word1',
      repetitions: 3,
      easeFactor: 2.5,
      interval: 7,
      nextReviewDate: '2026-08-20',
      lastReviewedAt: '2026-08-13T12:00:00Z',
      history: [{ date: '2026-08-13', grade: 4 }],
    },
  },
  settings: {
    dailyNewWordsLimit: 5,
    highContrast: false,
    reducedMotion: false,
    textSize: 'small',
  },
  devices: {
    'device-abc': {
      id: 'device-abc',
      name: 'Pixel 9 (Android API 35)',
      lastActive: '2026-08-13T12:00:00Z',
      createdAt: '2026-08-01T10:00:00Z',
    },
  },
  email: 'user@example.com',
  username: 'FilipTest',
  updatedAt: '2026-08-13T12:00:00Z',
};

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'slownik-test',
    firestore: {
      rules: readFileSync('firestore.rules', 'utf8'),
      host: 'localhost',
      port: 8080,
    },
  });
});

afterEach(async () => {
  await testEnv.clearFirestore();
});

afterAll(async () => {
  await testEnv.cleanup();
});

// ─── Pomocniki ────────────────────────────────────────────────────────────────

function ownerFirestore() {
  return testEnv.authenticatedContext(OWNER_UID).firestore();
}

function otherFirestore() {
  return testEnv.authenticatedContext(OTHER_UID).firestore();
}

function unauthFirestore() {
  return testEnv.unauthenticatedContext().firestore();
}

function ownerDocRef(db = ownerFirestore()) {
  return doc(db, 'users', OWNER_UID);
}

// ─── Testy: ODCZYT ───────────────────────────────────────────────────────────

describe('Odczyt dokumentu uzytkownika', () => {
  beforeAll(async () => {
    // Seedujemy dane przez kontekst admina (omija reguly)
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), 'users', OWNER_UID), VALID_USER_DOC);
    });
  });

  it('wlasciciel moze odczytac swoj dokument', async () => {
    await assertSucceeds(getDoc(ownerDocRef()));
  });

  it('inny zalogowany uzytkownik NIE moze odczytac cudzego dokumentu', async () => {
    const otherRef = doc(otherFirestore(), 'users', OWNER_UID);
    await assertFails(getDoc(otherRef));
  });

  it('niezalogowany NIE moze odczytac zadnego dokumentu', async () => {
    const unauthRef = doc(unauthFirestore(), 'users', OWNER_UID);
    await assertFails(getDoc(unauthRef));
  });
});

// ─── Testy: ZAPIS ────────────────────────────────────────────────────────────

describe('Zapis dokumentu uzytkownika', () => {
  it('wlasciciel moze zapisac poprawny dokument', async () => {
    await assertSucceeds(setDoc(ownerDocRef(), VALID_USER_DOC));
  });

  it('wlasciciel moze zaktualizowac tylko pole updatedAt', async () => {
    await assertSucceeds(
      setDoc(ownerDocRef(), { updatedAt: '2026-08-14T00:00:00Z' }, { merge: true })
    );
  });

  it('inny zalogowany uzytkownik NIE moze pisac do cudzego dokumentu', async () => {
    const otherRef = doc(otherFirestore(), 'users', OWNER_UID);
    await assertFails(setDoc(otherRef, VALID_USER_DOC));
  });

  it('niezalogowany NIE moze pisac do zadnego dokumentu', async () => {
    const unauthRef = doc(unauthFirestore(), 'users', OWNER_UID);
    await assertFails(setDoc(unauthRef, VALID_USER_DOC));
  });

  it('NIE mozna zapisac dokumentu z niedozwolonym polem (hacked_field)', async () => {
    const maliciousDoc = { ...VALID_USER_DOC, hacked_field: 'payload' };
    await assertFails(setDoc(ownerDocRef(), maliciousDoc));
  });

  it('NIE mozna tworzyc dokumentow pod cudzym UID', async () => {
    // Uzytkownik OWNER probuje stworzyc dokument pod UID OTHER
    const foreignRef = doc(ownerFirestore(), 'users', OTHER_UID);
    await assertFails(setDoc(foreignRef, VALID_USER_DOC));
  });
});

// ─── Testy: USUWANIE ─────────────────────────────────────────────────────────

describe('Usuwanie dokumentu uzytkownika', () => {
  beforeAll(async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), 'users', OWNER_UID), VALID_USER_DOC);
    });
  });

  it('wlasciciel moze usunac swoj dokument', async () => {
    // delete to specjalny przypadek - nie ma request.resource.data, wiec validUserDocument() nie obowiazuje
    // Jezeli regula write pokrywa delete i wymaga validUserDocument(), to ten test moze failowac.
    // W takim razie nalezy podzielic write na create/update/delete.
    await assertSucceeds(deleteDoc(ownerDocRef()));
  });

  it('inny uzytkownik NIE moze usunac cudzego dokumentu', async () => {
    const otherRef = doc(otherFirestore(), 'users', OWNER_UID);
    await assertFails(deleteDoc(otherRef));
  });
});

// ─── Testy: KOLEKCJE SPOZA /users ────────────────────────────────────────────

describe('Dostep do innych kolekcji', () => {
  it('nikt nie ma dostepu do kolekcji /admin', async () => {
    const adminRef = doc(ownerFirestore(), 'admin', 'secret');
    await assertFails(getDoc(adminRef));
  });

  it('nikt nie ma dostepu do kolekcji /config', async () => {
    const configRef = doc(ownerFirestore(), 'config', 'app');
    await assertFails(getDoc(configRef));
  });
});
