-- Run once in the Supabase SQL Editor.
-- The launcher inserts a profile after the user verifies/signs in.
-- Username is a protected profile row, not editable auth user_metadata.

create table if not exists public.akachi_profiles (
    user_id uuid primary key references auth.users (id) on delete cascade,
    minecraft_username text not null,
    created_at timestamptz not null default now(),
    constraint akachi_minecraft_username_format
        check (minecraft_username ~ '^[A-Za-z0-9_]{3,16}$')
);

create unique index if not exists akachi_profiles_username_ci_unique
    on public.akachi_profiles (lower(minecraft_username));

alter table public.akachi_profiles enable row level security;

revoke all on public.akachi_profiles from anon;
revoke update, delete on public.akachi_profiles from authenticated;
grant select, insert on public.akachi_profiles to authenticated;

drop policy if exists "Read own Akachi profile" on public.akachi_profiles;
create policy "Read own Akachi profile"
    on public.akachi_profiles
    for select
    to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "Create own Akachi profile once" on public.akachi_profiles;
create policy "Create own Akachi profile once"
    on public.akachi_profiles
    for insert
    to authenticated
    with check (
        (select auth.uid()) = user_id
        and minecraft_username ~ '^[A-Za-z0-9_]{3,16}$'
    );
