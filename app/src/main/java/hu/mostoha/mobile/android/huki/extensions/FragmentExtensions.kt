package hu.mostoha.mobile.android.huki.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

fun <T : Fragment> FragmentManager.addFragment(@IdRes containerId: Int, clazz: Class<T>) {
    commit {
        setCustomAnimations(
            android.R.anim.fade_in, android.R.anim.fade_out,
            android.R.anim.fade_in, android.R.anim.fade_out,
        )
        setReorderingAllowed(true)
        add(containerId, clazz, null)
        addToBackStack(null)
    }
}

fun FragmentManager.addFragment(@IdRes containerId: Int, fragment: Fragment) {
    commit {
        setReorderingAllowed(true)
        add(containerId, fragment, null)
    }
}

fun FragmentManager.removeFragments(@IdRes containerId: Int) {
    beginTransaction().apply {
        fragments.filter {
            it.id == containerId
        }.forEach {
            remove(it)
        }
    }.commit()
}
