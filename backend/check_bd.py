# test_db.py
import os
os.environ.setdefault("PYTHONUTF8", "1")

import psycopg

print("🔍 Перевірка підключення до safehomedb...\n")

try:
    conn = psycopg.connect(
        host="127.0.0.1",
        port=5432,
        dbname="safehomedb",
        user="postgres",
        password="11111111",
        autocommit=True
    )

    print("✅ ПІДКЛЮЧЕННЯ УСПІШНЕ!\n")

    with conn.cursor() as cur:
        cur.execute("SELECT version();")
        print("PostgreSQL:", cur.fetchone()[0])

        cur.execute("SELECT current_database();")
        print("База даних:", cur.fetchone()[0])

        cur.execute("SELECT current_user;")
        print("Користувач:", cur.fetchone()[0])

    conn.close()
    print("\n🎉 База даних готова до використання!")

except Exception as e:
    print("❌ Помилка підключення:")
    print(e)

input("\nНатисни Enter для виходу...")