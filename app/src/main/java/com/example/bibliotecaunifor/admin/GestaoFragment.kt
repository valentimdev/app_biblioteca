// app/src/main/java/.../admin/GestaoFragment.kt
package br.unifor.biblioteca.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotecaunifor.admin.GestaoViewModel
import com.example.bibliotecaunifor.admin.User
import com.example.bibliotecaunifor.admin.UserStatus
import com.example.bibliotecaunifor.databinding.FragmentAdminGestaoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.bibliotecaunifor.R
import com.example.bibliotecaunifor.admin.UsersAdapter
import com.google.android.material.appbar.MaterialToolbar

class GestaoFragment : Fragment() {

    private var _b: FragmentAdminGestaoBinding? = null
    private val b get() = _b!!
    private val vm: GestaoViewModel by viewModels()
    private val adapter = UsersAdapter(::onUserClick)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAdminGestaoBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        b.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        b.rvUsers.adapter = adapter

        b.etSearch.addTextChangedListener {
            vm.setQuery(it?.toString().orEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.users.collectLatest { adapter.submitList(it) }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.total.collectLatest { b.tvTotal.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.ativos.collectLatest { b.tvAtivos.text = it.toString() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            vm.bloqueados.collectLatest { b.tvBloqueados.text = it.toString() }
        }
    }

    private fun onUserClick(user: User) {
        val acao = if (user.status == UserStatus.ATIVO) "Bloquear" else "Desbloquear"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${user.nome}")
            .setMessage("Status: ${user.status.name}\n\n${acao} este usuário?")
            .setPositiveButton(acao) { _, _ -> vm.alternarStatus(user.id) }
            .setNegativeButton("Histórico") { _, _ ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Histórico")
                    .setMessage("Aqui entra a timeline de empréstimos e ações.\n(Mock sem backend)")
                    .setPositiveButton("Fechar", null)
                    .show()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = "GESTÃO"
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.top_app_bar)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> {
                    // Ação do sino
                    true
                }
                else -> false
            }
        }
    }

}
