package com.example.todolist.ui.nav

// Re-export the routes sealed class from the `routes` package to avoid duplicate definitions.
import com.example.todolist.ui.nav.routes.AppRoute as RoutesAppRoute

// Type alias so existing imports referencing `com.example.todolist.ui.nav.AppRoute` continue to work.
typealias AppRoute = RoutesAppRoute
